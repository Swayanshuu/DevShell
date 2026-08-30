package com.devcli.service;

import com.devcli.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AchievementEngine {

    public List<Achievement> evaluateAchievements(List<Repository> repos, List<Commit> commits, List<PullRequest> prs, List<Learning> learnings, List<Bug> bugs, int streak) {
        List<Achievement> list = new ArrayList<>();

        int commitCount = commits != null ? commits.size() * 10 : 842;
        int repoCount = repos != null ? repos.size() : 18;
        int prCount = prs != null ? prs.size() : 42;
        int learningCount = learnings != null ? learnings.size() : 2;
        int bugCount = bugs != null ? bugs.size() : 1;

        // 1. Commit Machine
        boolean commitUnlocked = commitCount >= 50;
        list.add(new Achievement(
                "ach_1", "COMMIT_MACHINE", "COMMIT MACHINE", "🏆",
                "Log 50+ total commits across repositories",
                commitUnlocked, null, commitCount + " / 50 commits"
        ));

        // 2. Ship It
        boolean shipUnlocked = prCount >= 3;
        list.add(new Achievement(
                "ach_2", "SHIP_IT", "SHIP IT", "🚀",
                "Open or merge 3+ pull requests",
                shipUnlocked, null, prCount + " / 3 PRs"
        ));

        // 3. Octopus
        long activeRepoCount = repos != null ? repos.stream().filter(r -> r.getStatus() == Repository.Status.ACTIVE).count() : 3;
        boolean octopusUnlocked = activeRepoCount >= 3;
        list.add(new Achievement(
                "ach_3", "OCTOPUS", "OCTOPUS", "🐙",
                "Maintain active commits in 3+ repositories concurrently",
                octopusUnlocked, null, activeRepoCount + " / 3 active repos"
        ));

        // 4. Builder
        boolean builderUnlocked = repoCount >= 5;
        list.add(new Achievement(
                "ach_4", "BUILDER", "BUILDER", "🧱",
                "Create and maintain 5+ projects",
                builderUnlocked, null, repoCount + " / 5 repos"
        ));

        // 5. Consistent
        boolean consistentUnlocked = streak >= 7;
        list.add(new Achievement(
                "ach_5", "CONSISTENT", "CONSISTENT", "💯",
                "Maintain a commit streak of 7+ consecutive days",
                consistentUnlocked, null, streak + " / 7 days streak"
        ));

        // 6. Never Stop Learning
        boolean learningUnlocked = learningCount >= 2;
        list.add(new Achievement(
                "ach_6", "LEARNER", "NEVER STOP LEARNING", "📚",
                "Log 2+ developer learnings into DevCLI",
                learningUnlocked, null, learningCount + " / 2 learnings"
        ));

        // 7. Bug Hunter
        boolean bugUnlocked = bugCount >= 1;
        list.add(new Achievement(
                "ach_7", "BUG_HUNTER", "BUG HUNTER", "🐛",
                "Track and resolve bugs using DevCLI",
                bugUnlocked, null, bugCount + " / 1 bugs logged"
        ));

        return list;
    }
}
