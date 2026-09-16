package com.devcli.service;

import com.devcli.model.Learning;
import com.devcli.storage.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private final LocalStorageService storageService;

    @Autowired
    public LearningService(LocalStorageService storageService) {
        this.storageService = storageService;
    }

    public List<Learning> getAllLearnings() {
        List<Learning> list = storageService.getLearnings();
        if (list == null) {
            list = new ArrayList<>();
        }
        boolean needsSave = migrateAndNormalizeIds(list);
        if (needsSave) {
            storageService.saveLearnings(list);
        }
        return list;
    }

    public Optional<Learning> findById(String idOrNumber) {
        if (idOrNumber == null || idOrNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        List<Learning> learnings = getAllLearnings();
        String searchKey = idOrNumber.trim().toUpperCase();
        
        // Direct match (e.g. LEARN-042)
        for (Learning l : learnings) {
            if (l.getId() != null && l.getId().equalsIgnoreCase(searchKey)) {
                return Optional.of(l);
            }
        }

        // Numeric match (e.g. "42" matching "LEARN-042")
        String numericOnly = searchKey.replaceAll("[^0-9]", "");
        if (!numericOnly.isEmpty()) {
            int num = Integer.parseInt(numericOnly);
            String formattedKey = String.format("LEARN-%03d", num);
            for (Learning l : learnings) {
                if (l.getId() != null && l.getId().equalsIgnoreCase(formattedKey)) {
                    return Optional.of(l);
                }
            }
        }

        return Optional.empty();
    }

    public Learning addLearning(String rawTitle, String content, List<String> tags, String project, String category) {
        List<Learning> learnings = getAllLearnings();

        String finalContent = content != null ? content.trim() : "";
        String finalTitle = rawTitle != null && !rawTitle.trim().isEmpty() ? rawTitle.trim() : deriveTitle(finalContent);
        String finalCategory = category != null && !category.trim().isEmpty() ? category.trim() : autoDetectCategory(finalTitle + " " + finalContent);
        List<String> finalTags = tags != null ? tags.stream().map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList()) : new ArrayList<>();
        String finalProject = project != null && !project.trim().isEmpty() ? project.trim() : "General";

        String newId = generateNextId(learnings);
        LocalDateTime now = LocalDateTime.now();

        Learning newLearning = new Learning(newId, finalTitle, finalContent, finalCategory, finalTags, finalProject, now, now, false);
        learnings.add(0, newLearning);
        storageService.saveLearnings(learnings);

        return newLearning;
    }

    public Learning updateLearning(String idOrNumber, String newTitle, String newContent, List<String> newTags, String newProject, String newCategory) {
        Optional<Learning> opt = findById(idOrNumber);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Learning with ID '" + idOrNumber + "' not found.");
        }

        Learning learning = opt.get();
        List<Learning> learnings = getAllLearnings();

        if (newTitle != null && !newTitle.trim().isEmpty()) {
            learning.setTitle(newTitle.trim());
        }
        if (newContent != null && !newContent.trim().isEmpty()) {
            learning.setContent(newContent.trim());
        }
        if (newTags != null) {
            List<String> cleanedTags = newTags.stream().map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList());
            learning.setTags(cleanedTags);
        }
        if (newProject != null && !newProject.trim().isEmpty()) {
            learning.setProject(newProject.trim());
        }
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            learning.setCategory(newCategory.trim());
        }

        learning.setUpdatedAt(LocalDateTime.now());

        // Save updated list
        for (int i = 0; i < learnings.size(); i++) {
            if (learnings.get(i).getId().equalsIgnoreCase(learning.getId())) {
                learnings.set(i, learning);
                break;
            }
        }
        storageService.saveLearnings(learnings);

        return learning;
    }

    public boolean deleteLearning(String idOrNumber) {
        Optional<Learning> opt = findById(idOrNumber);
        if (opt.isEmpty()) {
            return false;
        }
        Learning target = opt.get();
        List<Learning> learnings = getAllLearnings();
        boolean removed = learnings.removeIf(l -> l.getId().equalsIgnoreCase(target.getId()));
        if (removed) {
            storageService.saveLearnings(learnings);
        }
        return removed;
    }

    public boolean setPinned(String idOrNumber, boolean pinned) {
        Optional<Learning> opt = findById(idOrNumber);
        if (opt.isEmpty()) {
            return false;
        }
        Learning target = opt.get();
        target.setPinned(pinned);
        target.setUpdatedAt(LocalDateTime.now());

        List<Learning> learnings = getAllLearnings();
        for (int i = 0; i < learnings.size(); i++) {
            if (learnings.get(i).getId().equalsIgnoreCase(target.getId())) {
                learnings.set(i, target);
                break;
            }
        }
        storageService.saveLearnings(learnings);
        return true;
    }

    public List<Learning> searchLearnings(String query, String tagFilter, String projectFilter) {
        List<Learning> learnings = getAllLearnings();
        
        return learnings.stream().filter(l -> {
            boolean matchesQuery = true;
            if (query != null && !query.trim().isEmpty()) {
                String q = query.toLowerCase().trim();
                boolean titleMatch = l.getTitle() != null && l.getTitle().toLowerCase().contains(q);
                boolean contentMatch = l.getContent() != null && l.getContent().toLowerCase().contains(q);
                boolean tagMatch = l.getTags() != null && l.getTags().stream().anyMatch(t -> t.toLowerCase().contains(q));
                boolean projMatch = l.getProject() != null && l.getProject().toLowerCase().contains(q);
                boolean catMatch = l.getCategory() != null && l.getCategory().toLowerCase().contains(q);
                boolean idMatch = l.getId() != null && l.getId().toLowerCase().contains(q);

                matchesQuery = titleMatch || contentMatch || tagMatch || projMatch || catMatch || idMatch;
            }

            boolean matchesTag = true;
            if (tagFilter != null && !tagFilter.trim().isEmpty()) {
                String tFilter = tagFilter.toLowerCase().trim();
                matchesTag = l.getTags() != null && l.getTags().stream().anyMatch(t -> t.toLowerCase().equalsIgnoreCase(tFilter) || t.toLowerCase().contains(tFilter));
            }

            boolean matchesProject = true;
            if (projectFilter != null && !projectFilter.trim().isEmpty()) {
                String pFilter = projectFilter.toLowerCase().trim();
                matchesProject = l.getProject() != null && (l.getProject().toLowerCase().equalsIgnoreCase(pFilter) || l.getProject().toLowerCase().contains(pFilter));
            }

            return matchesQuery && matchesTag && matchesProject;
        }).collect(Collectors.toList());
    }

    public KnowledgeBaseStats getStats() {
        List<Learning> learnings = getAllLearnings();
        long pinnedCount = learnings.stream().filter(Learning::isPinned).count();
        long uniqueProjects = learnings.stream().map(Learning::getProject).filter(Objects::nonNull).distinct().count();
        
        Set<String> allTags = new HashSet<>();
        for (Learning l : learnings) {
            if (l.getTags() != null) {
                allTags.addAll(l.getTags());
            }
        }

        return new KnowledgeBaseStats(learnings.size(), (int) uniqueProjects, allTags.size(), (int) pinnedCount);
    }

    private boolean migrateAndNormalizeIds(List<Learning> list) {
        boolean modified = false;
        int maxIdNum = 0;

        // First pass: find max existing numeric ID formatted like LEARN-XXX
        for (Learning l : list) {
            if (l.getId() != null && l.getId().matches("(?i)LEARN-\\d+")) {
                int num = Integer.parseInt(l.getId().substring(6));
                maxIdNum = Math.max(maxIdNum, num);
            }
        }

        // Second pass: assign IDs to records with missing or UUID format IDs
        for (Learning l : list) {
            if (l.getId() == null || !l.getId().matches("(?i)LEARN-\\d+")) {
                maxIdNum++;
                l.setId(String.format("LEARN-%03d", maxIdNum));
                modified = true;
            }
            if (l.getTitle() == null || l.getTitle().trim().isEmpty()) {
                l.setTitle(deriveTitle(l.getContent()));
                modified = true;
            }
            if (l.getProject() == null || l.getProject().trim().isEmpty()) {
                l.setProject("General");
                modified = true;
            }
            if (l.getCategory() == null || l.getCategory().trim().isEmpty()) {
                l.setCategory(autoDetectCategory(l.getTitle() + " " + l.getContent()));
                modified = true;
            }
            if (l.getTags() == null) {
                l.setTags(new ArrayList<>());
                modified = true;
            }
            if (l.getCreatedAt() == null) {
                l.setCreatedAt(LocalDateTime.now());
                modified = true;
            }
            if (l.getUpdatedAt() == null) {
                l.setUpdatedAt(l.getCreatedAt());
                modified = true;
            }
        }
        return modified;
    }

    private String generateNextId(List<Learning> list) {
        int maxId = 0;
        for (Learning l : list) {
            if (l.getId() != null && l.getId().matches("(?i)LEARN-\\d+")) {
                try {
                    int num = Integer.parseInt(l.getId().substring(6));
                    maxId = Math.max(maxId, num);
                } catch (Exception ignored) {}
            }
        }
        return String.format("LEARN-%03d", maxId + 1);
    }

    public String deriveTitle(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Untitled Learning";
        }
        String firstLine = content.trim().split("\r?\n")[0];
        if (firstLine.length() <= 50) {
            return firstLine;
        }
        return firstLine.substring(0, 47) + "...";
    }

    public String autoDetectCategory(String text) {
        if (text == null) return "General";
        String lower = text.toLowerCase();
        if (lower.contains("cli") || lower.contains("picocli") || lower.contains("terminal") || lower.contains("cmd") || lower.contains("shell")) {
            return "CLI";
        }
        if (lower.contains("sql") || lower.contains("redis") || lower.contains("db") || lower.contains("postgres") || lower.contains("mongo")) {
            return "Database";
        }
        if (lower.contains("docker") || lower.contains("k8s") || lower.contains("git") || lower.contains("devops") || lower.contains("deploy") || lower.contains("npm")) {
            return "DevOps";
        }
        if (lower.contains("auth") || lower.contains("token") || lower.contains("jwt") || lower.contains("security") || lower.contains("oauth")) {
            return "Security";
        }
        if (lower.contains("react") || lower.contains("ui") || lower.contains("css") || lower.contains("html") || lower.contains("frontend") || lower.contains("vue")) {
            return "Frontend";
        }
        if (lower.contains("spring") || lower.contains("backend") || lower.contains("api") || lower.contains("webclient") || lower.contains("service") || lower.contains("java") || lower.contains("node")) {
            return "Backend";
        }
        return "General";
    }

    public static class KnowledgeBaseStats {
        public final int totalLearnings;
        public final int uniqueProjects;
        public final int totalTags;
        public final int pinnedCount;

        public KnowledgeBaseStats(int totalLearnings, int uniqueProjects, int totalTags, int pinnedCount) {
            this.totalLearnings = totalLearnings;
            this.uniqueProjects = uniqueProjects;
            this.totalTags = totalTags;
            this.pinnedCount = pinnedCount;
        }
    }
}
