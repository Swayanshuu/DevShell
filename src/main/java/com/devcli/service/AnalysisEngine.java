package com.devcli.service;

import com.devcli.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisEngine {

    public int calculateStreak(List<Commit> commits) {
        if (commits == null || commits.isEmpty()) return 0;

        Set<LocalDate> commitDates = commits.stream()
                .filter(c -> c.getDate() != null)
                .map(c -> c.getDate().toLocalDate())
                .collect(Collectors.toSet());

        LocalDate today = LocalDate.now();
        if (!commitDates.contains(today) && !commitDates.contains(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate current = commitDates.contains(today) ? today : today.minusDays(1);

        while (commitDates.contains(current)) {
            streak++;
            current = current.minusDays(1);
        }
        return streak;
    }

    public List<Commit> getTodayCommits(List<Commit> commits) {
        if (commits == null || commits.isEmpty()) return new ArrayList<>();
        LocalDate today = LocalDate.now();
        return commits.stream()
                .filter(c -> c.getDate() != null && c.getDate().toLocalDate().equals(today))
                .collect(Collectors.toList());
    }

    public String getCurrentlyBuildingProject(List<Repository> repos, List<Commit> commits) {
        if (commits != null && !commits.isEmpty()) {
            Map<String, Long> projectCommitCounts = commits.stream()
                    .collect(Collectors.groupingBy(Commit::getRepoName, Collectors.counting()));

            Optional<Map.Entry<String, Long>> topProject = projectCommitCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            if (topProject.isPresent()) {
                return topProject.get().getKey();
            }
        }

        if (repos != null && !repos.isEmpty()) {
            return repos.get(0).getName();
        }
        return "N/A";
    }

    public Map<String, Double> calculateLanguagePercentages(List<Repository> repos) {
        Map<String, Double> languageWeights = new HashMap<>();
        double total = 0;

        if (repos != null) {
            for (Repository repo : repos) {
                if (repo.getLanguages() != null && !repo.getLanguages().isEmpty()) {
                    for (Map.Entry<String, Long> entry : repo.getLanguages().entrySet()) {
                        languageWeights.put(entry.getKey(), languageWeights.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
                        total += entry.getValue();
                    }
                } else if (repo.getLanguage() != null && !repo.getLanguage().equalsIgnoreCase("Unknown")) {
                    double weight = repo.getCommitCount() > 0 ? repo.getCommitCount() * 1000 : 5000;
                    languageWeights.put(repo.getLanguage(), languageWeights.getOrDefault(repo.getLanguage(), 0.0) + weight);
                    total += weight;
                }
            }
        }

        if (total == 0) {
            return new LinkedHashMap<>();
        }

        final double finalTotal = total;
        Map<String, Double> percentages = new LinkedHashMap<>();
        languageWeights.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .forEach(e -> percentages.put(e.getKey(), Math.round((e.getValue() / finalTotal) * 1000.0) / 10.0));

        return percentages;
    }
}
