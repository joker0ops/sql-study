package com.sqlearn.dto;

import com.sqlearn.entity.Lesson;

public record LessonDto(Long id, Long courseId, String title, String url, Integer sortOrder, boolean completed) {

    public static LessonDto from(Lesson lesson, boolean completed) {
        return new LessonDto(lesson.getId(), lesson.getCourseId(), lesson.getTitle(),
                lesson.getUrl(), lesson.getSortOrder(), completed);
    }
}
