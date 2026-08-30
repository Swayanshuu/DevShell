package com.devcli.service;

import com.devcli.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InsightEngine {

    public List<Insight> generateInsights(List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages) {
        List<Insight> insights = new ArrayList<>();

        // 1. Activity Concentration Insight
        if (commits != null && !commits.isEmpty()) {
            Map<String, Long> projectCounts = commits.stream()
                    .collect(Collectors.groupingBy(Commit::getRepoName, Collectors.counting()));

            long totalCommits = commits.size();
            projectCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(top -> {
                        int pct = (int) Math.round(((double) top.getValue() / totalCommits) * 100);
                        insights.add(new Insight(
                                "PROJECT FOCUS",
                                top.getKey() + " Concentration",
                                pct + "% of your recent commits are concentrated in " + top.getKey() + ". Your recent activity is strongly focused on this primary project.",
                                pct + "% activity density",
                                5
                        ));
                    });
        }

        // 2. Tech Stack Insight
        if (languages != null && !languages.isEmpty()) {
            List<Map.Entry<String, Double>> topLangs = languages.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .limit(2)
                    .collect(Collectors.toList());

            if (!topLangs.isEmpty()) {
                String primary = topLangs.get(0).getKey();
                double pct = topLangs.get(0).getValue();
                String secondary = topLangs.size() > 1 ? " & " + topLangs.get(1).getKey() : "";
                
                insights.add(new Insight(
                        "STACK SPECIALLIZATION",
                        primary + secondary + " Dominance",
                        primary + " accounts for " + pct + "% of your repository language distribution. You maintain a high specialization in " + primary + ".",
                        pct + "% stack share",
                        4
                ));
            }
        }

        // 3. Maintenance Insight
        if (repos != null && !repos.isEmpty()) {
            LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
            long inactiveCount = repos.stream()
                    .filter(r -> r.getUpdatedAt() != null && r.getUpdatedAt().isBefore(sixMonthsAgo))
                    .count();

            if (inactiveCount > 0) {
                insights.add(new Insight(
                        "REPOSITORY HEALTH",
                        "Maintenance Opportunity",
                        inactiveCount + " of your repositories have not received commits in over 6 months. Consider archiving or refreshing stale projects.",
                        inactiveCount + " stale repos",
                        3
                ));
            } else {
                insights.add(new Insight(
                        "REPOSITORY HEALTH",
                        "Active Maintenance",
                        "All of your active projects have received updates within the last 6 months. Excellent repository upkeep!",
                        "100% active upkeep",
                        3
                ));
            }
        }

        // 4. Contribution Flow Insight
        if (prs != null && !prs.isEmpty()) {
            long openPRs = prs.stream().filter(pr -> "OPEN".equalsIgnoreCase(pr.getState())).count();
            insights.add(new Insight(
                    "PULL REQUEST WORKFLOW",
                    "Code Review Velocity",
                    "You have " + openPRs + " active pull requests awaiting review/merge. Maintaining small, frequent PRs keeps your delivery cycle fast.",
                    openPRs + " open PRs",
                    4
            ));
        }

        return insights;
    }

    public String getRandomObservation(List<Repository> repos, List<Commit> commits) {
        if (commits != null && !commits.isEmpty()) {
            String topRepo = commits.get(0).getRepoName();
            return "You've been working on " + topRepo + " consistently this week. 🔥";
        }
        return "Your commit velocity has been steady over the past fortnight. Keep building! 🚀";
    }
}
