package com.sqlearn.controller;

import com.sqlearn.dto.ApiResponse;
import com.sqlearn.dto.ProgressSummaryDto;
import com.sqlearn.dto.ToggleResult;
import com.sqlearn.service.ProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/summary")
    public ApiResponse<List<ProgressSummaryDto>> summary(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(progressService.summary(userId));
    }

    @GetMapping("/course/{courseId}")
    public ApiResponse<List<Long>> courseProgress(@RequestAttribute("userId") Long userId,
                                                  @PathVariable Long courseId) {
        return ApiResponse.ok(progressService.completedLessonIds(userId, courseId));
    }

    @PostMapping("/{lessonId}/toggle")
    public ApiResponse<ToggleResult> toggle(@RequestAttribute("userId") Long userId,
                                            @PathVariable Long lessonId) {
        return ApiResponse.ok(progressService.toggle(userId, lessonId));
    }
}
