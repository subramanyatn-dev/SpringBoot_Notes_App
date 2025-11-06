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

import com.app.notes.model.Stream;
import com.app.notes.service.StreamService;

@RestController
@RequestMapping("/streams")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    // GET /streams - Get all streams
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Stream> streams = streamService.getAll();
        return ResponseEntity.ok(streams);
    }

    // GET /streams/{id} - Get specific stream // not being used !!!!
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Stream stream = streamService.getById(id);
            return ResponseEntity.ok(stream);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // POST /streams - Create new stream (ADMIN only)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        String name = body.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name is required"));
        }
        
        Stream stream = streamService.create(name);
        return ResponseEntity.ok(stream);
    }


    // DELETE /streams/{id} - Delete stream (ADMIN only)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body(Map.of("message", "Admins only"));
        }
        
        try {
            streamService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
