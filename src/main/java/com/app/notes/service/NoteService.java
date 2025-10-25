package com.app.notes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.notes.dto.NoteResponse;
import com.app.notes.model.Note;
import com.app.notes.model.Subject;
import com.app.notes.repository.NoteRepository;

@Service
public class NoteService {

    private final NoteRepository repository;
    private final SubjectService subjectService;
    private final StorageService storageService;

    public NoteService(NoteRepository repository, SubjectService subjectService, StorageService storageService) {
        this.repository = repository;
        this.subjectService = subjectService;
        this.storageService = storageService;
    }

    public List<NoteResponse> getBySubjectId(Long subjectId) {
        List<Note> notes = repository.findBySubjectId(subjectId);
        return notes.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public NoteResponse create(Long subjectId, String title, MultipartFile file) throws Exception {
        Subject subject = subjectService.getById(subjectId);
        if (subject == null) throw new Exception("Subject not found");

        // Build GCS path: Stream/Semester/Subject/filename
        String path = String.format("%s/%d/%s/%s",
            subject.getSemester().getStream().getName(),
            subject.getSemester().getNumber(),
            subject.getName(),
            file.getOriginalFilename()
        );

        String fileUrl = storageService.uploadFile(file, path);
        
        Note note = new Note(title, fileUrl, subject);
        Note saved = repository.save(note);
        return toResponse(saved);
    }

    public void delete(String id) throws Exception {
        if (!repository.existsById(id)) throw new Exception("Note not found");
        repository.deleteById(id);
    }

    public NoteResponse getById(String id) throws Exception {
        Note note = repository.findById(id).orElseThrow(() -> new Exception("Note not found"));
        return toResponse(note);
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(
            note.getId(),
            note.getTitle(),
            note.getFileUrl(),
            note.getSubject().getId(),
            note.getSubject().getName()
        );
    }
}
