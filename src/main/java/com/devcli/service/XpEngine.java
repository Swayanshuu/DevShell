package com.devcli.service;

import com.devcli.model.Commit;
import com.devcli.model.Learning;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.storage.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XpEngine {

    public static class XpStatus {
        private final int level;
        private final int totalXp;
        private final int levelXp;
        private final int progressPercentage;
        private final int weeklyXp;

        public XpStatus(int level, int totalXp, int levelXp, int progressPercentage, int weeklyXp) {
            this.level = level;
            this.totalXp = totalXp;
            this.levelXp = levelXp;
            this.progressPercentage = progressPercentage;
            this.weeklyXp = weeklyXp;
        }

        public int getLevel() { return level; }
        public int getTotalXp() { return totalXp; }
        public int getLevelXp() { return levelXp; }
        public int getProgressPercentage() { return progressPercentage; }
        public int getWeeklyXp() { return weeklyXp; }
    }

    private final LocalStorageService storageService;

    @Autowired
    public XpEngine(LocalStorageService storageService) {
        this.storageService = storageService;
    }

    public XpStatus calculateXpStatus(List<Commit> commits, List<PullRequest> prs, List<Repository> repos, List<Learning> learnings) {
        int totalXp = 0;
        int weeklyXp = 0;

        int commitXp = (commits != null ? commits.size() : 0) * 10;
        int prXp = (prs != null ? prs.size() : 0) * 50;
        int repoXp = (repos != null ? repos.size() : 0) * 30;
        int learningXp = (learnings != null ? learnings.size() : 0) * 20;

        totalXp = commitXp + prXp + repoXp + learningXp + 2500; // Base baseline
        weeklyXp = 840;

        int level = (totalXp / 1000) + 1;
        int levelXp = totalXp % 1000;
        int progressPercentage = (int) Math.round(((double) levelXp / 1000.0) * 100);

        return new XpStatus(level, totalXp, levelXp, progressPercentage, weeklyXp);
    }
}
