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

import com.app.notes.model.Semester;
import com.app.notes.service.SemesterService;

@RestController
@RequestMapping("/streams/{streamId}/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    // GET /streams/{streamId}/semesters - Get all semesters for a stream
    @GetMapping
    public ResponseEntity<?> getByStream(@PathVariable Long streamId) {
        try {
            List<Semester> semesters = semesterService.getByStreamId(streamId);
            return ResponseEntity.ok(semesters);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // GET /streams/{streamId}/semesters/{id} - Get specific semester
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long streamId, @PathVariable Long id) {
        try {
            Semester semester = semesterService.getById(id);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /streams/{streamId}/semesters - Create new semester (ADMIN only)
    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long streamId, @RequestBody Map<String, Integer> body, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        Integer number = body.get("number");
        if (number == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Semester number is required"));
        }
        
        try {
            Semester semester = semesterService.create(streamId, number);
            return ResponseEntity.ok(semester);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // DELETE /streams/{streamId}/semesters/{id} - Delete semester (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long streamId, @PathVariable Long id, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        try {
            semesterService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
