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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.notes.model.Subject;
import com.app.notes.service.SubjectService;

@RestController
@RequestMapping("/semesters/{semesterId}/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // GET /semesters/{semesterId}/subjects - Get all subjects for a semester
    @GetMapping
    public ResponseEntity<?> getBySemester(@PathVariable Long semesterId) {
        try {
            List<Subject> subjects = subjectService.getBySemesterId(semesterId);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // GET /semesters/{semesterId}/subjects/{id} - Get specific subject
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long semesterId, @PathVariable Long id) {
        try {
            Subject subject = subjectService.getById(id);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /semesters/{semesterId}/subjects - Create new subject (ADMIN only)
    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long semesterId, @RequestBody Map<String, String> body, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        String name = body.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Subject name is required"));
        }
        
        try {
            Subject subject = subjectService.create(semesterId, name);
            return ResponseEntity.ok(subject);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /semesters/{semesterId}/subjects/{id} - Delete subject (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long semesterId, @PathVariable Long id, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        try {
            subjectService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
