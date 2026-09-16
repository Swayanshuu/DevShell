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
@Command(name = "portfolio", description = "Generate developer portfolio and export to HTML", mixinStandardHelpOptions = true)
public class PortfolioCommand implements Runnable {

    @Option(names = {"--export"}, description = "Export format (e.g. html)")
    private String exportFormat;

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public PortfolioCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
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
        int projectsCount = (int) repos.stream().filter(r -> r.getStatus() == Repository.Status.ACTIVE || r.getStatus() == Repository.Status.RECENTLY_ACTIVE).count();
        int reposCount = repos.size();
        int commitsCount = commits.size();
        int prsCount = prs.size();
        int osCount = (int) repos.stream().filter(r -> !r.isPrivate()).count();

        if ("html".equalsIgnoreCase(exportFormat)) {
            String path = ReportExporter.exportPortfolioHtml(username, projectsCount, reposCount, commitsCount, prsCount, osCount, languages);
            System.out.println(AnsiStyle.boldGreen("\n✓ Exported Developer Portfolio HTML to: ") + AnsiStyle.boldCyan(path) + "\n");
            return;
        }

        System.out.println();
        List<String> portLines = new java.util.ArrayList<>();
        portLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Active Projects")), AnsiStyle.boldWhite(String.valueOf(projectsCount))));
        portLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Total Repositories")), AnsiStyle.boldWhite(String.valueOf(reposCount))));
        portLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Total Commits")), AnsiStyle.boldWhite(String.format("%,d", commitsCount))));
        portLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Pull Requests")), AnsiStyle.boldWhite(String.valueOf(prsCount))));
        portLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Open Source Repos")), AnsiStyle.boldWhite(String.valueOf(osCount))));

        if (!languages.isEmpty()) {
            portLines.add("");
            portLines.add("  " + AnsiStyle.boldWhite("PRIMARY STACK"));
            languages.keySet().stream().limit(4).forEach(lang -> portLines.add("    " + AnsiStyle.cyan(String.format("%-18s", lang)) + AnsiStyle.boldYellow(String.format("%.0f%%", languages.get(lang)))));
        }

        BoxRenderer.renderBox("DEVELOPER PORTFOLIO OVERVIEW", portLines, AnsiStyle.CYAN);
        System.out.println("  " + AnsiStyle.dim("Export to HTML: `devshell portfolio --export html`\n"));
    }
}
