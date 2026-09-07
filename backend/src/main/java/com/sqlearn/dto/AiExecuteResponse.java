package com.sqlearn.dto;

public record AiExecuteResponse(
        SqlResult result,
        Boolean correct,
        String feedback,
        String suggestions,
        String referenceSql
) {
}
