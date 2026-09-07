package com.sqlearn.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlearn.entity.Course;
import com.sqlearn.entity.Lesson;
import com.sqlearn.repository.CourseRepository;
import com.sqlearn.repository.LessonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

/**
 * 首次启动时初始化教程数据（来源于菜鸟教程数据库板块）。
 * 仅当课程表为空时执行，避免重复插入。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(CourseRepository courseRepository,
                           LessonRepository lessonRepository,
                           ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (courseRepository.count() > 0) {
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/courses.json");
        List<SeedCourse> courses;
        try (InputStream in = resource.getInputStream()) {
            courses = objectMapper.readValue(in, new TypeReference<List<SeedCourse>>() {
            });
        }

        int courseOrder = 0;
        for (SeedCourse sc : courses) {
            courseOrder++;
            Course course = new Course();
            course.setCode(sc.code());
            course.setName(sc.name());
            course.setDescription(sc.description());
            course.setSortOrder(courseOrder);
            courseRepository.save(course);

            int lessonOrder = 0;
            for (SeedLesson sl : sc.lessons()) {
                lessonOrder++;
                Lesson lesson = new Lesson();
                lesson.setCourseId(course.getId());
                lesson.setTitle(sl.title());
                lesson.setUrl(sl.url());
                lesson.setSortOrder(lessonOrder);
                lessonRepository.save(lesson);
            }
        }
    }

    public record SeedCourse(String code, String name, String description, List<SeedLesson> lessons) {
    }

    public record SeedLesson(String title, String url) {
    }
}
