package com.app.notes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.notes.model.Semester;
import com.app.notes.model.Stream;
import com.app.notes.repository.SemesterRepository;

@Service
public class SemesterService {

    private final SemesterRepository repository;
    private final StreamService streamService;

    public SemesterService(SemesterRepository repository, StreamService streamService) {
        this.repository = repository;
        this.streamService = streamService;
    }

    public List<Semester> getByStreamId(Long streamId) {
        return repository.findByStreamId(streamId);
    }

    public Semester create(Long streamId, Integer number) throws Exception {
        Stream stream = streamService.getById(streamId);
        
        Semester semester = new Semester();
        semester.setNumber(number);
        semester.setStream(stream);
        return repository.save(semester);
    }

    public Semester getById(Long id) throws Exception {
        return repository.findById(id).orElseThrow(() -> new Exception("Semester not found"));
    }

    public void delete(Long id) throws Exception {
        if (!repository.existsById(id)) throw new Exception("Semester not found");
        repository.deleteById(id);
    }
}
