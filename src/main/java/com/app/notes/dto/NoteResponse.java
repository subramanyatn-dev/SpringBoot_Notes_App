package com.app.notes.dto;

public class NoteResponse {
    private String id;
    private String title;
    private String fileUrl;
    private Long subjectId;
    private String subjectName;

    public NoteResponse() {}

    public NoteResponse(String id, String title, String fileUrl, Long subjectId, String subjectName) {
        this.id = id;
        this.title = title;
        this.fileUrl = fileUrl;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}
