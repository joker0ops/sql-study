package com.sqlearn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiConfigRequest(
        @NotBlank(message = "API 地址不能为空") String apiUrl,
        @Size(max = 500, message = "API 密钥过长") String apiKey,
        @NotBlank(message = "模型名称不能为空") String modelName
) {
}
