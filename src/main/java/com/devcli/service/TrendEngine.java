package com.devcli.service;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrendEngine {

    public static class TrendMetrics {
        private final int days;
        private final int currentCommits;
        private final int previousCommits;
        private final double commitChangePct;
        private final int currentPrs;
        private final int previousPrs;
        private final double prChangePct;
        private final int activeRepos;
        private final Map<String, Double> topLanguages;

        public TrendMetrics(int days, int currentCommits, int previousCommits, double commitChangePct, int currentPrs, int previousPrs, double prChangePct, int activeRepos, Map<String, Double> topLanguages) {
            this.days = days;
            this.currentCommits = currentCommits;
            this.previousCommits = previousCommits;
            this.commitChangePct = commitChangePct;
            this.currentPrs = currentPrs;
            this.previousPrs = previousPrs;
            this.prChangePct = prChangePct;
            this.activeRepos = activeRepos;
            this.topLanguages = topLanguages;
        }

        public int getDays() { return days; }
        public int getCurrentCommits() { return currentCommits; }
        public int getPreviousCommits() { return previousCommits; }
        public double getCommitChangePct() { return commitChangePct; }
        public int getCurrentPrs() { return currentPrs; }
        public int getPreviousPrs() { return previousPrs; }
        public double getPrChangePct() { return prChangePct; }
        public int getActiveRepos() { return activeRepos; }
        public Map<String, Double> getTopLanguages() { return topLanguages; }
    }

    public TrendMetrics calculateTrend(int days, List<Commit> commits, List<PullRequest> prs, List<Repository> repos, AnalysisEngine analysisEngine) {
        LocalDate today = LocalDate.now();
        LocalDate periodStart = today.minusDays(days);
        LocalDate prevPeriodStart = periodStart.minusDays(days);

        int currentCommits = 0;
        int previousCommits = 0;

        if (commits != null) {
            for (Commit c : commits) {
                if (c.getDate() != null) {
                    LocalDate d = c.getDate().toLocalDate();
                    if (!d.isBefore(periodStart) && !d.isAfter(today)) {
                        currentCommits++;
                    } else if (!d.isBefore(prevPeriodStart) && d.isBefore(periodStart)) {
                        previousCommits++;
                    }
                }
            }
        }

        int currentPrs = 0;
        int previousPrs = 0;
        if (prs != null) {
            for (PullRequest p : prs) {
                if (p.getCreatedAt() != null) {
                    LocalDate d = p.getCreatedAt().toLocalDate();
                    if (!d.isBefore(periodStart) && !d.isAfter(today)) {
                        currentPrs++;
                    } else if (!d.isBefore(prevPeriodStart) && d.isBefore(periodStart)) {
                        previousPrs++;
                    }
                }
            }
        }

        double commitChangePct = previousCommits > 0
                ? Math.round(((double) (currentCommits - previousCommits) / previousCommits) * 100.0)
                : (currentCommits > 0 ? 100.0 : 0.0);

        double prChangePct = previousPrs > 0
                ? Math.round(((double) (currentPrs - previousPrs) / previousPrs) * 100.0)
                : (currentPrs > 0 ? 100.0 : 0.0);

        Set<String> activeRepoNames = new HashSet<>();
        if (commits != null) {
            for (Commit c : commits) {
                if (c.getDate() != null && !c.getDate().toLocalDate().isBefore(periodStart)) {
                    activeRepoNames.add(c.getRepoName());
                }
            }
        }

        Map<String, Double> topLanguages = analysisEngine.calculateLanguagePercentages(repos);

        return new TrendMetrics(days, currentCommits, previousCommits, commitChangePct, currentPrs, previousPrs, prChangePct, activeRepoNames.size(), topLanguages);
    }
}
