package com.app.notes.dto;

public record NoteResponse(
    String id,
    String title,
    String fileUrl,
    Long subjectId,
    String subjectName
) {}
