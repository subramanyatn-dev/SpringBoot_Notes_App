package com.app.notes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.notes.dto.NoteResponse;
import com.app.notes.service.NoteService;

@RestController
@RequestMapping("/subjects/{subjectId}/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // GET /subjects/{subjectId}/notes - Get all notes for a subject
    @GetMapping
    public ResponseEntity<?> getBySubject(@PathVariable Long subjectId) {
        try {
            List<NoteResponse> notes = noteService.getBySubjectId(subjectId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // GET /subjects/{subjectId}/notes/{id} - Get specific note
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long subjectId, @PathVariable String id) {
        try {
            NoteResponse note = noteService.getById(id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /subjects/{subjectId}/notes - Upload a new note (ADMIN only)
    @PostMapping
    public ResponseEntity<?> create(
            @PathVariable Long subjectId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        try {
            NoteResponse note = noteService.create(subjectId, title, file);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /subjects/{subjectId}/notes/{id} - Delete note (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long subjectId, 
            @PathVariable String id, 
            Authentication auth) {
        
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        try {
            noteService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
