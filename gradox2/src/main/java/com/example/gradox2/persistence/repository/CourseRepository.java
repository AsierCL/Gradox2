package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.gradox2.persistence.entities.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

}
