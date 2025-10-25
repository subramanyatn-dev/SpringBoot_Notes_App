package com.app.notes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.notes.model.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
    List<Note> findBySubjectId(Long subjectId);
}
