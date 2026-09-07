package com.sqlearn.dto;

import java.util.List;

/**
 * 用户 SQL 在专属库中的执行结果。error 为空表示执行成功。
 */
public record SqlResult(List<String> columns, List<List<Object>> rows, int rowCount, String error) {
}
