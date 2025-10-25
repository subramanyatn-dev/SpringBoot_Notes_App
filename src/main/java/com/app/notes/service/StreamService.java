package com.app.notes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.notes.model.Stream;
import com.app.notes.repository.StreamRepository;

@Service
public class StreamService {

    private final StreamRepository repository;

    public StreamService(StreamRepository repository) {
        this.repository = repository;
    }

    public List<Stream> getAll() {
        return repository.findAll();
    }

    public Stream create(String name) {
        Stream stream = new Stream();
        stream.setName(name);
        return repository.save(stream);
    }

    public Stream getById(Long id) throws Exception {
        return repository.findById(id).orElseThrow(() -> new Exception("Stream not found"));
    }

    public void delete(Long id) throws Exception {
        if (!repository.existsById(id)) throw new Exception("Stream not found");
        repository.deleteById(id);
    }
}
