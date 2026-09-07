package com.sqlearn.dto;

public record ProgressSummaryDto(Long courseId, String code, String name,
                                 long totalLessons, long completedLessons, int percentage) {
}
