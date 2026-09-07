package com.sqlearn.repository;

import com.sqlearn.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByOrderBySortOrderAsc();
}
