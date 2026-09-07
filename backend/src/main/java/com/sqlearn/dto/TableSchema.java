package com.sqlearn.dto;

import java.util.List;

/**
 * 练习库中某张表的结构信息（表名、列名、行数）。
 */
public record TableSchema(String table, List<String> columns, long rowCount) {
}
