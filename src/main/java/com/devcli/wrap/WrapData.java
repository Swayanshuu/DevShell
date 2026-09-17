package com.devcli.wrap;

public record WrapData(
        String period,
        int commits,
        double codingHours,
        int additions,
        int deletions,
        int linesChanged,
        int activeDays,
        int longestStreak,
        int projects,
        String topLanguage,
        String topRepository,
        String username,
        String displayName,
        String avatarUrl) {
}