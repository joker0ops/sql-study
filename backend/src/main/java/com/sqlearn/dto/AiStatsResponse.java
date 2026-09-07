package com.sqlearn.dto;

import java.util.List;

public record AiStatsResponse(List<HeatmapDay> heatmap, AccuracyStats accuracy) {
}
