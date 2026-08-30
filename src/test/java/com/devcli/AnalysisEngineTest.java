package com.devcli;

import com.devcli.model.Commit;
import com.devcli.model.Repository;
import com.devcli.service.AnalysisEngine;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnalysisEngineTest {

    @Test
    public void testCalculateStreak() {
        AnalysisEngine engine = new AnalysisEngine();
        List<Commit> commits = new ArrayList<>();
        commits.add(new Commit("1", "LinkPeer", "feat", "dev", "email", LocalDateTime.now(), "url"));
        commits.add(new Commit("2", "LinkPeer", "fix", "dev", "email", LocalDateTime.now().minusDays(1), "url"));

        int streak = engine.calculateStreak(commits);
        assertTrue(streak >= 2, "Streak should be at least 2 days");
    }

    @Test
    public void testLanguagePercentages() {
        AnalysisEngine engine = new AnalysisEngine();
        List<Repository> repos = new ArrayList<>();

        Repository r1 = new Repository();
        r1.setName("LinkPeer");
        Map<String, Long> l1 = new HashMap<>();
        l1.put("Java", 7000L);
        l1.put("TypeScript", 3000L);
        r1.setLanguages(l1);
        repos.add(r1);

        Map<String, Double> pcts = engine.calculateLanguagePercentages(repos);
        assertEquals(70.0, pcts.get("Java"), 0.1);
        assertEquals(30.0, pcts.get("TypeScript"), 0.1);
    }

    @Test
    public void testEmptyDataReturnsZero() {
        AnalysisEngine engine = new AnalysisEngine();
        int streak = engine.calculateStreak(new ArrayList<>());
        assertEquals(0, streak, "Empty commits should result in 0 streak");

        Map<String, Double> pcts = engine.calculateLanguagePercentages(new ArrayList<>());
        assertTrue(pcts.isEmpty(), "Empty repositories should result in empty language map");
    }
}
