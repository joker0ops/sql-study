package com.sqlearn.service;

import com.sqlearn.dto.CourseDetailDto;
import com.sqlearn.dto.CourseDto;
import com.sqlearn.dto.LessonDto;
import com.sqlearn.entity.Course;
import com.sqlearn.entity.Lesson;
import com.sqlearn.exception.BizException;
import com.sqlearn.repository.CourseRepository;
import com.sqlearn.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public CourseService(CourseRepository courseRepository, LessonRepository lessonRepository) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<CourseDto> listCourses() {
        return courseRepository.findByOrderBySortOrderAsc().stream()
                .map(c -> CourseDto.from(c, lessonRepository.countByCourseId(c.getId())))
                .toList();
    }

    public CourseDetailDto getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BizException("课程不存在"));
        List<LessonDto> lessons = lessonRepository.findByCourseIdOrderBySortOrderAsc(courseId).stream()
                .map(l -> LessonDto.from(l, false))
                .toList();
        return new CourseDetailDto(course.getId(), course.getCode(), course.getName(),
                course.getDescription(), course.getSortOrder(), lessons);
    }
}
