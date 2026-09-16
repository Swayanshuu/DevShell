package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.model.UserProfile;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

@Component
@Command(name = "wrapped", description = "Yearly development recap summary", mixinStandardHelpOptions = true)
public class WrappedCommand implements Runnable {

    @Option(names = {"--export"}, description = "Export format (e.g. html)")
    private String exportFormat;

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public WrappedCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        UserProfile user = storageService.getUserProfile();
        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);

        String username = user != null && user.getUsername() != null ? user.getUsername() : "Developer";
        int totalCommits = commits.size();
        String topLang = languages.isEmpty() ? "N/A" : languages.keySet().iterator().next().toUpperCase();
        String topRepo = analysisEngine.getCurrentlyBuildingProject(repos, commits);
        int longestStreak = analysisEngine.calculateLongestStreak(commits);
        int techCount = languages.size();
        int shippedCount = (int) repos.stream().filter(r -> !r.isPrivate()).count();
        int prsCount = prs.size();

        if ("html".equalsIgnoreCase(exportFormat)) {
            String path = ReportExporter.exportWrappedHtml(username, totalCommits, topLang, topRepo, longestStreak, techCount, shippedCount, prsCount);
            System.out.println(AnsiStyle.boldGreen("\n✓ Exported 2026 Developer Wrapped HTML report to: ") + AnsiStyle.boldCyan(path) + "\n");
            return;
        }

        System.out.println();
        List<String> wrappedLines = new java.util.ArrayList<>();
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Commits")), AnsiStyle.boldWhite(String.format("%,d", totalCommits))));
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Top Language")), AnsiStyle.boldYellow(topLang)));
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Most Active Project")), AnsiStyle.boldCyan(topRepo != null ? topRepo : "N/A")));
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Longest Streak")), AnsiStyle.boldGreen(longestStreak + " DAYS")));
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Technologies Explored")), AnsiStyle.boldMagenta(String.valueOf(techCount))));
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Public Repositories")), AnsiStyle.boldWhite(String.valueOf(shippedCount))));
        wrappedLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Pull Requests")), AnsiStyle.boldYellow(String.valueOf(prsCount))));

        BoxRenderer.renderBox("2026 DEVELOPER WRAPPED SUMMARY", wrappedLines, AnsiStyle.MAGENTA);
        System.out.println("  " + AnsiStyle.dim("Export to HTML: `devshell wrapped --export html`\n"));
    }
}
