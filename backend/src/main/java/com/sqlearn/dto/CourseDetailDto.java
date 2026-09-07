package com.sqlearn.dto;

import java.util.List;

public record CourseDetailDto(Long id, String code, String name, String description,
                              Integer sortOrder, List<LessonDto> lessons) {
}
