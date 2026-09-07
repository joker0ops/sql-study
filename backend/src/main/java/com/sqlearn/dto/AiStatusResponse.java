package com.sqlearn.dto;

import java.util.List;

public record AiStatusResponse(
        boolean configured,
        String apiUrl,
        String maskedApiKey,
        String modelName,
        boolean databaseCreated,
        List<TableSchema> schema,
        List<TableRelation> relations
) {
}
