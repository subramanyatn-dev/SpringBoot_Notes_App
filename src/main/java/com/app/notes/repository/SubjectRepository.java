package com.app.notes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.notes.model.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySemesterId(Long semesterId);
}
