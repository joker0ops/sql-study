package com.sqlearn.dto;

/**
 * 表中某一列的信息（列名 + 友好化类型）。
 */
public record ColumnInfo(String name, String type) {
}
