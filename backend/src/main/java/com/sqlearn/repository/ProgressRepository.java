package com.sqlearn.repository;

import com.sqlearn.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<Progress> findByUserIdAndCompletedTrue(Long userId);

    long countByUserIdAndLessonIdInAndCompletedTrue(Long userId, Collection<Long> lessonIds);
}
