package com.devcli;

import com.devcli.model.Commit;
import com.devcli.model.Repository;
import com.devcli.service.AnalysisEngine;
import com.devcli.service.DeveloperDnaEngine;
import com.devcli.service.TrendEngine;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DeveloperOsEnginesTest {

    @Test
    public void testDeveloperDnaAnalysis() {
        DeveloperDnaEngine engine = new DeveloperDnaEngine();
        AnalysisEngine analysisEngine = new AnalysisEngine();

        List<Repository> repos = new ArrayList<>();
        Repository r1 = new Repository();
        r1.setName("dev-backend");
        r1.setLanguage("Java");
        r1.setCommitCount(50);
        repos.add(r1);

        Repository r2 = new Repository();
        r2.setName("flutter-mobile");
        r2.setLanguage("Dart");
        r2.setCommitCount(30);
        repos.add(r2);

        DeveloperDnaEngine.DnaProfile profile = engine.analyzeDna(repos, new ArrayList<>(), new ArrayList<>(), analysisEngine);

        assertNotNull(profile);
        assertNotNull(profile.getPrimaryDomain());
        assertTrue(profile.getCategoryBreakdown().containsKey("Backend"));
        assertTrue(profile.getCategoryBreakdown().containsKey("Mobile"));
    }

    @Test
    public void testTrendEngineMetrics() {
        TrendEngine trendEngine = new TrendEngine();
        AnalysisEngine analysisEngine = new AnalysisEngine();

        List<Commit> commits = new ArrayList<>();
        commits.add(new Commit("1", "DevShell", "feat: initial", "author", "email", LocalDateTime.now(), "url"));
        commits.add(new Commit("2", "DevShell", "feat: update", "author", "email", LocalDateTime.now().minusDays(5), "url"));

        TrendEngine.TrendMetrics metrics = trendEngine.calculateTrend(30, commits, new ArrayList<>(), new ArrayList<>(), analysisEngine);

        assertNotNull(metrics);
        assertEquals(2, metrics.getCurrentCommits());
        assertEquals(30, metrics.getDays());
    }
}
