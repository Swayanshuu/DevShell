package com.devcli.storage;

import com.devcli.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocalStorageService {

    private final Path baseDir;
    private final ObjectMapper mapper;

    public LocalStorageService() {
        this.baseDir = Paths.get(System.getProperty("user.home"), ".devshell");
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ensureBaseDir();
    }

    private void ensureBaseDir() {
        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
            }
        } catch (Exception ignored) {}
    }

    public void saveUserProfile(UserProfile userProfile) {
        saveToFile("profile.json", userProfile);
    }

    public UserProfile getUserProfile() {
        return readFromFile("profile.json", UserProfile.class);
    }

    public boolean hasUserProfile() {
        File file = baseDir.resolve("profile.json").toFile();
        return file.exists();
    }

    public void clearData() {
        try {
            File profileFile = baseDir.resolve("profile.json").toFile();
            if (profileFile.exists()) profileFile.delete();
            File reposFile = baseDir.resolve("repositories.json").toFile();
            if (reposFile.exists()) reposFile.delete();
            File commitsFile = baseDir.resolve("commits.json").toFile();
            if (commitsFile.exists()) commitsFile.delete();
            File prsFile = baseDir.resolve("pull_requests.json").toFile();
            if (prsFile.exists()) prsFile.delete();
        } catch (Exception ignored) {}
    }

    public void saveRepositories(List<Repository> repositories) {
        saveToFile("repositories.json", repositories);
    }

    public List<Repository> getRepositories() {
        List<Repository> list = readListFromFile("repositories.json", new TypeReference<List<Repository>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void saveCommits(List<Commit> commits) {
        saveToFile("commits.json", commits);
    }

    public List<Commit> getCommits() {
        List<Commit> list = readListFromFile("commits.json", new TypeReference<List<Commit>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void savePullRequests(List<PullRequest> pullRequests) {
        saveToFile("pull_requests.json", pullRequests);
    }

    public List<PullRequest> getPullRequests() {
        List<PullRequest> list = readListFromFile("pull_requests.json", new TypeReference<List<PullRequest>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void saveIssues(List<Issue> issues) {
        saveToFile("issues.json", issues);
    }

    public List<Issue> getIssues() {
        List<Issue> list = readListFromFile("issues.json", new TypeReference<List<Issue>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void saveEvents(List<ActivityEvent> events) {
        saveToFile("events.json", events);
    }

    public List<ActivityEvent> getEvents() {
        List<ActivityEvent> list = readListFromFile("events.json", new TypeReference<List<ActivityEvent>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void saveLearnings(List<Learning> learnings) {
        saveToFile("learnings.json", learnings);
    }

    public List<Learning> getLearnings() {
        List<Learning> list = readListFromFile("learnings.json", new TypeReference<List<Learning>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void saveBugs(List<Bug> bugs) {
        saveToFile("bugs.json", bugs);
    }

    public List<Bug> getBugs() {
        List<Bug> list = readListFromFile("bugs.json", new TypeReference<List<Bug>>() {});
        return list != null ? list : new ArrayList<>();
    }

    public void saveAchievements(List<Achievement> achievements) {
        saveToFile("achievements.json", achievements);
    }

    public List<Achievement> getAchievements() {
        List<Achievement> list = readListFromFile("achievements.json", new TypeReference<List<Achievement>>() {});
        return list != null ? list : new ArrayList<>();
    }

    private <T> void saveToFile(String filename, T object) {
        try {
            ensureBaseDir();
            File target = baseDir.resolve(filename).toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(target, object);
        } catch (Exception ignored) {}
    }

    private <T> T readFromFile(String filename, Class<T> clazz) {
        try {
            File target = baseDir.resolve(filename).toFile();
            if (!target.exists()) return null;
            return mapper.readValue(target, clazz);
        } catch (IOException e) {
            return null;
        }
    }

    private <T> List<T> readListFromFile(String filename, TypeReference<List<T>> typeRef) {
        try {
            File target = baseDir.resolve(filename).toFile();
            if (!target.exists()) return null;
            return mapper.readValue(target, typeRef);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void deleteFile(String filename) {
        try {
            Files.deleteIfExists(baseDir.resolve(filename));
        } catch (IOException ignored) {}
    }
}
