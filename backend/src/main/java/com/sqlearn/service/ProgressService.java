package com.sqlearn.service;

import com.sqlearn.dto.ProgressSummaryDto;
import com.sqlearn.dto.ToggleResult;
import com.sqlearn.entity.Course;
import com.sqlearn.entity.Lesson;
import com.sqlearn.entity.Progress;
import com.sqlearn.exception.BizException;
import com.sqlearn.repository.CourseRepository;
import com.sqlearn.repository.LessonRepository;
import com.sqlearn.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    public ProgressService(ProgressRepository progressRepository,
                           CourseRepository courseRepository,
                           LessonRepository lessonRepository) {
        this.progressRepository = progressRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
    }

    public List<ProgressSummaryDto> summary(Long userId) {
        return courseRepository.findByOrderBySortOrderAsc().stream()
                .map(course -> buildSummary(userId, course))
                .toList();
    }

    private ProgressSummaryDto buildSummary(Long userId, Course course) {
        List<Long> lessonIds = lessonRepository.findByCourseIdOrderBySortOrderAsc(course.getId()).stream()
                .map(Lesson::getId)
                .toList();
        long total = lessonIds.size();
        long completed = total == 0
                ? 0
                : progressRepository.countByUserIdAndLessonIdInAndCompletedTrue(userId, lessonIds);
        int percentage = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        return new ProgressSummaryDto(course.getId(), course.getCode(), course.getName(),
                total, completed, percentage);
    }

    public List<Long> completedLessonIds(Long userId, Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new BizException("课程不存在");
        }
        return progressRepository.findByUserIdAndCompletedTrue(userId).stream()
                .filter(p -> lessonRepository.findById(p.getLessonId())
                        .map(l -> courseId.equals(l.getCourseId()))
                        .orElse(false))
                .map(Progress::getLessonId)
                .toList();
    }

    public ToggleResult toggle(Long userId, Long lessonId) {
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BizException("课程章节不存在"));

        Progress progress = progressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    Progress p = new Progress();
                    p.setUserId(userId);
                    p.setLessonId(lessonId);
                    p.setCompleted(false);
                    return p;
                });

        progress.setCompleted(!progress.getCompleted());
        progress.setCompletedAt(progress.getCompleted() ? LocalDateTime.now() : null);
        progressRepository.save(progress);

        return new ToggleResult(lessonId, progress.getCompleted());
    }
}
