package com.app.notes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.notes.model.Semester;

public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByStreamId(Long streamId);
}
