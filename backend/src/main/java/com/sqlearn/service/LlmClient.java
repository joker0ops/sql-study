package com.sqlearn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlearn.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容的 LLM 客户端，调用 POST {apiUrl}/chat/completions。
 */
@Service
public class LlmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public LlmClient(ObjectMapper objectMapper,
                     @Value("${app.ai.insecure-ssl:false}") boolean insecureSsl) {
        this.objectMapper = objectMapper;
        if (insecureSsl) {
            trustAllCertificates();
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    /**
     * 信任所有 HTTPS 证书并关闭主机名校验，仅用于内网自签名证书的接口。
     */
    private static void trustAllCertificates() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAll, new SecureRandom());
            SSLContext.setDefault(sc);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new IllegalStateException("无法配置信任所有证书", e);
        }
    }

    public String chat(String apiUrl, String apiKey, String model, String systemPrompt, String userPrompt) {
        return chat(apiUrl, apiKey, model, systemPrompt, userPrompt, null, null);
    }

    public String chat(String apiUrl, String apiKey, String model, String systemPrompt, String userPrompt,
                       Integer maxTokens) {
        return chat(apiUrl, apiKey, model, systemPrompt, userPrompt, maxTokens, null);
    }

    public String chat(String apiUrl, String apiKey, String model, String systemPrompt, String userPrompt,
                       Integer maxTokens, Double temperature) {
        String url = apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        body.put("temperature", temperature != null ? temperature : 0.3);
        if (maxTokens != null) {
            body.put("max_tokens", maxTokens);
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            String resp = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBody)
                    .retrieve()
                    .body(String.class);

            JsonNode node = objectMapper.readTree(resp);
            JsonNode content = node.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new BizException("AI 返回内容为空，请检查模型与接口配置");
            }
            return content.asText();
        } catch (RestClientResponseException e) {
            String detail = e.getResponseBodyAsString();
            if (detail != null && detail.length() > 300) {
                detail = detail.substring(0, 300);
            }
            throw new BizException("AI 服务返回错误(" + e.getStatusCode().value() + "): "
                    + (detail == null || detail.isBlank() ? e.getMessage() : detail));
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("调用 AI 服务失败: " + e.getMessage());
        }
    }
}
