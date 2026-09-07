package com.sqlearn.dto;

import java.util.List;

/**
 * 练习库中某张表的结构信息（表名、列、行数、示例数据）。
 */
public record TableSchema(String table, List<ColumnInfo> columns, long rowCount, List<List<Object>> sampleRows) {
}
