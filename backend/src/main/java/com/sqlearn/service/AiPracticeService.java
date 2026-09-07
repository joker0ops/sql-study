package com.sqlearn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlearn.dto.AccuracyDay;
import com.sqlearn.dto.AccuracyStats;
import com.sqlearn.dto.AiConfigRequest;
import com.sqlearn.dto.AiExecuteRequest;
import com.sqlearn.dto.AiExecuteResponse;
import com.sqlearn.dto.AiQuestionResponse;
import com.sqlearn.dto.AiStatsResponse;
import com.sqlearn.dto.AiStatusResponse;
import com.sqlearn.dto.HeatmapDay;
import com.sqlearn.dto.SqlResult;
import com.sqlearn.dto.TableSchema;
import com.sqlearn.entity.AiConfig;
import com.sqlearn.entity.ExerciseRecord;
import com.sqlearn.exception.BizException;
import com.sqlearn.repository.AiConfigRepository;
import com.sqlearn.repository.ExerciseRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public class AiPracticeService {

    private static final String QUESTION_SYSTEM = "你是一位专业的 SQL 教学助手。请根据给定的数据库表结构，出一道 SQL 练习题让用户编写 SQL 语句完成。"
            + "题目覆盖常用知识点（查询、过滤、聚合、连接、排序、分组、子查询等），只用简体中文出题。"
            + "严格只输出一个 JSON 对象，不要输出任何多余文字或代码块，格式："
            + "{\"question\":\"题目\",\"difficulty\":\"简单|中等|困难\",\"hint\":\"提示\",\"tables\":[\"用到的表\"]}";

    private static final String JUDGE_SYSTEM = "你是一位 SQL 教学助手。请判断用户针对给定题目所写 SQL 是否正确，并给出反馈与改进建议。"
            + "严格只输出一个 JSON 对象，不要输出任何多余文字或代码块，格式："
            + "{\"correct\":true或false,\"feedback\":\"点评\",\"suggestions\":\"改进或优化建议\",\"referenceSql\":\"参考答案 SQL\"}";

    private final AiConfigRepository aiConfigRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final PracticeDbService practiceDbService;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public AiPracticeService(AiConfigRepository aiConfigRepository,
                             ExerciseRecordRepository exerciseRecordRepository,
                             PracticeDbService practiceDbService,
                             LlmClient llmClient,
                             ObjectMapper objectMapper) {
        this.aiConfigRepository = aiConfigRepository;
        this.exerciseRecordRepository = exerciseRecordRepository;
        this.practiceDbService = practiceDbService;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    public AiStatusResponse getStatus(Long userId) {
        Optional<AiConfig> configOpt = aiConfigRepository.findByUserId(userId);
        boolean configured = configOpt.isPresent();
        boolean databaseCreated = practiceDbService.isInitialized(userId);
        List<TableSchema> schema = databaseCreated ? practiceDbService.getSchema(userId) : List.of();
        if (configured) {
            AiConfig c = configOpt.get();
            return new AiStatusResponse(true, c.getApiUrl(), maskKey(c.getApiKey()),
                    c.getModelName(), databaseCreated, schema);
        }
        return new AiStatusResponse(false, null, null, null, databaseCreated, schema);
    }

    public void saveConfig(Long userId, AiConfigRequest req) {
        AiConfig config = aiConfigRepository.findByUserId(userId).orElseGet(AiConfig::new);
        LocalDateTime now = LocalDateTime.now();
        if (config.getId() == null) {
            config.setUserId(userId);
            config.setCreatedAt(now);
        }
        config.setApiUrl(req.apiUrl().trim());
        config.setApiKey(req.apiKey().trim());
        config.setModelName(req.modelName().trim());
        config.setUpdatedAt(now);
        aiConfigRepository.save(config);
    }

    public void init(Long userId) {
        practiceDbService.initDatabase(userId);
    }

    public void reset(Long userId) {
        practiceDbService.resetDatabase(userId);
    }

    public AiQuestionResponse generateQuestion(Long userId) {
        AiConfig config = requireConfig(userId);
        practiceDbService.initDatabase(userId);
        String schemaDesc = buildSchemaDescription(practiceDbService.getSchema(userId));

        String content = llmClient.chat(config.getApiUrl(), config.getApiKey(), config.getModelName(),
                QUESTION_SYSTEM, "数据库表结构如下：\n" + schemaDesc + "\n请出题。");

        JsonNode node = parseJson(content);
        String question = text(node, "question");
        if (question.isBlank()) {
            throw new BizException("AI 出题失败，请重试");
        }
        List<String> tables = new ArrayList<>();
        if (node.has("tables") && node.get("tables").isArray()) {
            node.get("tables").forEach(t -> tables.add(t.asText()));
        }
        return new AiQuestionResponse(question, text(node, "difficulty"), text(node, "hint"), tables);
    }

    public AiExecuteResponse execute(Long userId, AiExecuteRequest req) {
        AiConfig config = requireConfig(userId);
        practiceDbService.initDatabase(userId);
        SqlResult result = practiceDbService.executeSql(userId, req.userSql());
        String execDesc = buildExecutionDescription(result);

        String content;
        try {
            content = llmClient.chat(config.getApiUrl(), config.getApiKey(), config.getModelName(),
                    JUDGE_SYSTEM, "题目：" + req.question() + "\n用户 SQL：" + req.userSql()
                            + "\n执行情况：" + execDesc + "\n请判断是否正确并给出建议。");
        } catch (BizException e) {
            // LLM 失败：不写做题记录，仅返回执行结果与错误信息
            return new AiExecuteResponse(result, null, "AI 判题失败：" + e.getMessage(), null, null);
        }

        JsonNode node = parseJson(content);
        Boolean correct = node.hasNonNull("correct") ? node.get("correct").asBoolean() : null;
        if (correct == null) {
            correct = result.error() == null;
        }
        String feedback = text(node, "feedback");
        if (feedback.isBlank()) {
            feedback = content;
        }
        record(userId, req, result, correct, feedback);
        return new AiExecuteResponse(result, correct, feedback,
                text(node, "suggestions"), text(node, "referenceSql"));
    }

    public AiStatsResponse stats(Long userId) {
        List<ExerciseRecord> records = exerciseRecordRepository.findByUserIdOrderByCreatedAtAsc(userId);
        Map<LocalDate, long[]> byDay = new TreeMap<>();
        long total = 0;
        long correctTotal = 0;
        for (ExerciseRecord r : records) {
            LocalDate d = r.getCreatedAt().toLocalDate();
            long[] arr = byDay.computeIfAbsent(d, k -> new long[2]);
            arr[0]++;
            if (Boolean.TRUE.equals(r.getCorrect())) {
                arr[1]++;
                correctTotal++;
            }
            total++;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<HeatmapDay> heatmap = new ArrayList<>();
        List<AccuracyDay> series = new ArrayList<>();
        for (Map.Entry<LocalDate, long[]> e : byDay.entrySet()) {
            String date = e.getKey().format(fmt);
            long t = e.getValue()[0];
            long c = e.getValue()[1];
            heatmap.add(new HeatmapDay(date, (int) t));
            double pct = t == 0 ? 0 : Math.round(c * 1000.0 / t) / 10.0;
            series.add(new AccuracyDay(date, pct, t, c));
        }
        double overall = total == 0 ? 0 : Math.round(correctTotal * 1000.0 / total) / 10.0;
        return new AiStatsResponse(heatmap, new AccuracyStats(overall, total, correctTotal, series));
    }

    private AiConfig requireConfig(Long userId) {
        return aiConfigRepository.findByUserId(userId)
                .orElseThrow(() -> new BizException("请先配置 AI 服务"));
    }

    private void record(Long userId, AiExecuteRequest req, SqlResult result, Boolean correct, String feedback) {
        ExerciseRecord rec = new ExerciseRecord();
        rec.setUserId(userId);
        rec.setQuestion(req.question());
        rec.setUserSql(req.userSql());
        rec.setExecutionResult(buildExecutionDescription(result));
        rec.setCorrect(correct != null && correct);
        rec.setFeedback(feedback);
        rec.setCreatedAt(LocalDateTime.now());
        exerciseRecordRepository.save(rec);
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 4) {
            return "****";
        }
        return "****" + key.substring(key.length() - 4);
    }

    private String buildSchemaDescription(List<TableSchema> schema) {
        StringBuilder sb = new StringBuilder();
        for (TableSchema t : schema) {
            sb.append("表 ").append(t.table()).append("（").append(t.rowCount()).append(" 行），列：")
                    .append(String.join(", ", t.columns())).append("\n");
        }
        return sb.toString();
    }

    private String buildExecutionDescription(SqlResult r) {
        if (r.error() != null) {
            return "SQL 执行出错：" + r.error();
        }
        if (r.columns().isEmpty()) {
            return "SQL 执行成功，影响 " + r.rowCount() + " 行。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SQL 执行成功，返回 ").append(r.rowCount()).append(" 行，列：")
                .append(String.join(", ", r.columns()));
        int shown = Math.min(r.rows().size(), 10);
        for (int i = 0; i < shown; i++) {
            sb.append("\n行").append(i + 1).append(": ").append(r.rows().get(i));
        }
        return sb.toString();
    }

    private JsonNode parseJson(String content) {
        String s = content.trim();
        if (s.startsWith("```")) {
            int end = s.indexOf("```", 3);
            if (end > 0) {
                s = s.substring(3, end).trim();
            }
        }
        int brace = s.indexOf('{');
        if (brace > 0) {
            s = s.substring(brace);
        }
        try {
            return objectMapper.readTree(s);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n == null || n.isNull() ? "" : n.asText();
    }
}
