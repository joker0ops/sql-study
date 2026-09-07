package com.sqlearn.controller;

import com.sqlearn.dto.ApiResponse;
import com.sqlearn.dto.CourseDetailDto;
import com.sqlearn.dto.CourseDto;
import com.sqlearn.service.CourseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<CourseDto>> list() {
        return ApiResponse.ok(courseService.listCourses());
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(courseService.getCourse(id));
    }
}
