package com.devcli.wrap;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrapCalculationTest {

    @Test
    public void testCalculateLongestStreak() {
        WrapService wrapService = new WrapService(null, null);

        Set<LocalDate> dates = new HashSet<>();
        dates.add(LocalDate.of(2026, 1, 1));
        dates.add(LocalDate.of(2026, 1, 2));
        dates.add(LocalDate.of(2026, 1, 3));
        dates.add(LocalDate.of(2026, 1, 5));
        dates.add(LocalDate.of(2026, 1, 6));

        int longestStreak = wrapService.calculateLongestStreak(dates);
        assertEquals(3, longestStreak);
    }

    @Test
    public void testCalculateLongestStreakEmpty() {
        WrapService wrapService = new WrapService(null, null);
        assertEquals(0, wrapService.calculateLongestStreak(new HashSet<>()));
    }

    @Test
    public void testFindTopLanguage() {
        WrapService wrapService = new WrapService(null, null);

        List<Repository> repos = new ArrayList<>();
        Repository r1 = new Repository();
        r1.setName("DevShell");
        r1.setLanguage("Java");
        repos.add(r1);

        Repository r2 = new Repository();
        r2.setName("WebFront");
        r2.setLanguage("TypeScript");
        repos.add(r2);

        List<Commit> commits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Commit c = new Commit();
            c.setRepoName("DevShell");
            commits.add(c);
        }
        for (int i = 0; i < 2; i++) {
            Commit c = new Commit();
            c.setRepoName("WebFront");
            commits.add(c);
        }

        String topLang = wrapService.findTopLanguage(commits, repos);
        assertEquals("Java", topLang);
    }

    @Test
    public void testFindTopRepository() {
        WrapService wrapService = new WrapService(null, null);

        List<Repository> repos = new ArrayList<>();
        Repository r1 = new Repository();
        r1.setName("DevShell");
        repos.add(r1);

        Repository r2 = new Repository();
        r2.setName("DevCli");
        repos.add(r2);

        List<Commit> commits = new ArrayList<>();
        Commit c1 = new Commit();
        c1.setRepoName("DevShell");
        commits.add(c1);

        Commit c2 = new Commit();
        c2.setRepoName("DevShell");
        commits.add(c2);

        List<PullRequest> prs = new ArrayList<>();
        PullRequest pr1 = new PullRequest();
        pr1.setRepoName("DevCli");
        prs.add(pr1);

        String topRepo = wrapService.findTopRepository(commits, prs, repos);
        assertEquals("DevShell", topRepo);
    }

    @Test
    public void testFormatNumber() {
        assertEquals("42", WrapRenderer.formatNumber(42));
        assertEquals("999", WrapRenderer.formatNumber(999));
        assertEquals("1,842", WrapRenderer.formatNumber(1842));
        assertEquals("12.4K", WrapRenderer.formatNumber(12400));
        assertEquals("128.4K", WrapRenderer.formatNumber(128400));
        assertEquals("1.2M", WrapRenderer.formatNumber(1200000));
        assertEquals("12.8M", WrapRenderer.formatNumber(12800000));
    }

    @Test
    public void testCalculateCodingHours() {
        WrapService wrapService = new WrapService(null, null);
        List<Commit> commits = new ArrayList<>();

        LocalDateTime base = LocalDateTime.of(2026, 1, 10, 10, 0);
        Commit c1 = new Commit();
        c1.setDate(base);
        commits.add(c1);

        Commit c2 = new Commit();
        c2.setDate(base.plusMinutes(45));
        commits.add(c2);

        Commit c3 = new Commit();
        c3.setDate(base.plusHours(5)); // Separate session
        commits.add(c3);

        double hours = wrapService.calculateCodingHours(commits);
        assertEquals(1.8, hours, 0.1);
    }
}
