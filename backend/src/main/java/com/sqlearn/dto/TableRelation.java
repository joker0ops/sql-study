package com.sqlearn.dto;

/**
 * 表关系：table.column -> refTable.refColumn。
 */
public record TableRelation(String table, String column, String refTable, String refColumn) {
}
