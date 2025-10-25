package com.app.notes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.notes.model.Stream;

public interface StreamRepository extends JpaRepository<Stream, Long> {
}
