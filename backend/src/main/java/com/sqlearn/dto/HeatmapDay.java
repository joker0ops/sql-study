package com.sqlearn.dto;

/**
 * 打卡热力图的一天：date 为 yyyy-MM-dd，count 为当天做题次数。
 */
public record HeatmapDay(String date, int count) {
}
