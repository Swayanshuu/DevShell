package com.devcli.service;

import com.devcli.model.Bug;
import com.devcli.model.Learning;
import com.devcli.storage.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JournalService {

    private final LocalStorageService storageService;

    @Autowired
    public JournalService(LocalStorageService storageService) {
        this.storageService = storageService;
    }

    public List<Learning> getLearnings() {
        return storageService.getLearnings();
    }

    public void addLearning(String title, String category, String description) {
        List<Learning> list = storageService.getLearnings();
        list.add(0, new Learning(UUID.randomUUID().toString(), title, category != null ? category : "General", description != null ? description : "", LocalDateTime.now()));
        storageService.saveLearnings(list);
    }

    public List<Bug> getBugs() {
        return storageService.getBugs();
    }

    public void addBug(String title, String project, String severity, String notes) {
        List<Bug> list = storageService.getBugs();
        list.add(0, new Bug(UUID.randomUUID().toString(), title, project != null ? project : "General", severity != null ? severity : "MEDIUM", "OPEN", notes != null ? notes : "", LocalDateTime.now()));
        storageService.saveBugs(list);
    }

    public boolean resolveBug(String titleOrId) {
        List<Bug> list = storageService.getBugs();
        for (Bug b : list) {
            if (b.getId().equals(titleOrId) || b.getTitle().equalsIgnoreCase(titleOrId) || b.getTitle().toLowerCase().contains(titleOrId.toLowerCase())) {
                b.setStatus("RESOLVED");
                b.setResolvedAt(LocalDateTime.now());
                storageService.saveBugs(list);
                return true;
            }
        }
        return false;
    }
}
