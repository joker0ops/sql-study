package com.sqlearn.dto;

import jakarta.validation.constraints.NotBlank;

public record AiExecuteRequest(
        @NotBlank(message = "题目不能为空") String question,
        @NotBlank(message = "SQL 不能为空") String userSql
) {
}
