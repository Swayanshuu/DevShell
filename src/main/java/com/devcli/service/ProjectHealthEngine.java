package com.devcli.service;

import com.devcli.model.Commit;
import com.devcli.model.ProjectHealth;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectHealthEngine {

    public ProjectHealth evaluateHealth(Repository repo, List<Commit> commits, List<PullRequest> prs) {
        String repoName = repo.getName();
        List<Commit> repoCommits = commits != null ? commits.stream()
                .filter(c -> c.getRepoName().equalsIgnoreCase(repoName))
                .collect(Collectors.toList()) : new ArrayList<>();

        List<PullRequest> repoPrs = prs != null ? prs.stream()
                .filter(p -> p.getRepoName() != null && p.getRepoName().equalsIgnoreCase(repoName))
                .collect(Collectors.toList()) : new ArrayList<>();

        int commitCount = repo.getCommitCount() > 0 ? repo.getCommitCount() : repoCommits.size();
        int prCount = repoPrs.size();
        int openIssues = repo.getOpenIssuesCount();

        LocalDateTime lastActivity = repo.getLastCommitAt();
        if (lastActivity == null && !repoCommits.isEmpty() && repoCommits.get(0).getDate() != null) {
            lastActivity = repoCommits.get(0).getDate();
        }
        if (lastActivity == null) {
            lastActivity = repo.getUpdatedAt();
        }

        ProjectHealth.State state;
        if (lastActivity == null) {
            state = ProjectHealth.State.MAINTAINED;
        } else {
            long daysSince = ChronoUnit.DAYS.between(lastActivity, LocalDateTime.now());
            if (daysSince <= 14) {
                state = ProjectHealth.State.ACTIVE;
            } else if (daysSince <= 60) {
                state = ProjectHealth.State.MAINTAINED;
            } else if (daysSince <= 180) {
                state = ProjectHealth.State.LOW_ACTIVITY;
            } else {
                state = ProjectHealth.State.DORMANT;
            }
        }

        int contributorCount = (int) repoCommits.stream()
                .map(Commit::getAuthor)
                .filter(a -> a != null && !a.isEmpty())
                .distinct()
                .count();
        if (contributorCount == 0) contributorCount = 1;

        return new ProjectHealth(repoName, commitCount, contributorCount, openIssues, prCount, lastActivity, state);
    }
}
