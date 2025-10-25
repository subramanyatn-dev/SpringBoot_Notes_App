package com.app.notes.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.notes.model.Semester;
import com.app.notes.model.Subject;
import com.app.notes.repository.SubjectRepository;

@Service
public class SubjectService {

    private final SubjectRepository repository;
    private final SemesterService semesterService;

    public SubjectService(SubjectRepository repository, SemesterService semesterService) {
        this.repository = repository;
        this.semesterService = semesterService;
    }

    public List<Subject> getBySemesterId(Long semesterId) {
        return repository.findBySemesterId(semesterId);
    }

    public Subject create(Long semesterId, String name) throws Exception {
        Semester semester = semesterService.getById(semesterId);
        
        Subject subject = new Subject();
        subject.setName(name);
        subject.setSemester(semester);
        return repository.save(subject);
    }

    public Subject getById(Long id) throws Exception {
        return repository.findById(id).orElseThrow(() -> new Exception("Subject not found"));
    }

    public void delete(Long id) throws Exception {
        if (!repository.existsById(id)) throw new Exception("Subject not found");
        repository.deleteById(id);
    }
}
