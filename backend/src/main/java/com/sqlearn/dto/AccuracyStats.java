package com.sqlearn.dto;

import java.util.List;

public record AccuracyStats(double overall, long total, long correct, List<AccuracyDay> series) {
}
