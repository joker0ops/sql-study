package com.sqlearn.controller;

import com.sqlearn.dto.AiConfigRequest;
import com.sqlearn.dto.AiExecuteRequest;
import com.sqlearn.dto.AiExecuteResponse;
import com.sqlearn.dto.AiQuestionResponse;
import com.sqlearn.dto.AiStatsResponse;
import com.sqlearn.dto.AiStatusResponse;
import com.sqlearn.dto.ApiResponse;
import com.sqlearn.service.AiPracticeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiPracticeService aiPracticeService;

    public AiController(AiPracticeService aiPracticeService) {
        this.aiPracticeService = aiPracticeService;
    }

    @GetMapping("/status")
    public ApiResponse<AiStatusResponse> status(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(aiPracticeService.getStatus(userId));
    }

    @PostMapping("/config")
    public ApiResponse<Void> config(@RequestAttribute("userId") Long userId,
                                    @Valid @RequestBody AiConfigRequest request) {
        aiPracticeService.saveConfig(userId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/test")
    public ApiResponse<Void> test(@RequestAttribute("userId") Long userId,
                                  @Valid @RequestBody AiConfigRequest request) {
        aiPracticeService.testConnection(userId, request);
        return ApiResponse.ok();
    }

    @PostMapping("/init")
    public ApiResponse<Void> init(@RequestAttribute("userId") Long userId) {
        aiPracticeService.init(userId);
        return ApiResponse.ok();
    }

    @PostMapping("/reset")
    public ApiResponse<Void> reset(@RequestAttribute("userId") Long userId) {
        aiPracticeService.reset(userId);
        return ApiResponse.ok();
    }

    @PostMapping("/question")
    public ApiResponse<AiQuestionResponse> question(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(aiPracticeService.generateQuestion(userId));
    }

    @PostMapping("/execute")
    public ApiResponse<AiExecuteResponse> execute(@RequestAttribute("userId") Long userId,
                                                  @Valid @RequestBody AiExecuteRequest request) {
        return ApiResponse.ok(aiPracticeService.execute(userId, request));
    }

    @GetMapping("/stats")
    public ApiResponse<AiStatsResponse> stats(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(aiPracticeService.stats(userId));
    }
}
