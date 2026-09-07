package com.sqlearn.dto;

import jakarta.validation.constraints.NotBlank;

public record AiConfigRequest(
        @NotBlank(message = "API 地址不能为空") String apiUrl,
        @NotBlank(message = "API 密钥不能为空") String apiKey,
        @NotBlank(message = "模型名称不能为空") String modelName
) {
}
