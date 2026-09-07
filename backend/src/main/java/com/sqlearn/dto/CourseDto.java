package com.sqlearn.dto;

import com.sqlearn.entity.Course;

public record CourseDto(Long id, String code, String name, String description, Integer sortOrder, long lessonCount) {

    public static CourseDto from(Course course, long lessonCount) {
        return new CourseDto(course.getId(), course.getCode(), course.getName(),
                course.getDescription(), course.getSortOrder(), lessonCount);
    }
}
