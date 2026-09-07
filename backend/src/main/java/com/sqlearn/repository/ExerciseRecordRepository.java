package com.sqlearn.repository;

import com.sqlearn.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    List<ExerciseRecord> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<ExerciseRecord> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}
