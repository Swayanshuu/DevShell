package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.service.AnalysisEngine;
import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;

@Component
@Command(name = "report", description = "Generate development activity reports", mixinStandardHelpOptions = true)
public class ReportCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Report timeframe (weekly, monthly, yearly)")
    private String timeframe = "weekly";

    @Option(names = {"--format"}, description = "Output format (terminal, markdown, json, html)")
    private String format = "terminal";

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public ReportCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);

        String period = timeframe != null ? timeframe.toUpperCase() : "WEEKLY";

        if ("json".equalsIgnoreCase(format)) {
            System.out.println("{\"report\":\"" + period + "\",\"commits\":" + commits.size() + ",\"repos\":" + repos.size() + ",\"prs\":" + prs.size() + "}");
            return;
        }

        if ("markdown".equalsIgnoreCase(format)) {
            System.out.println("# DEVSHELL " + period + " REPORT\n\n- **Commits**: " + commits.size() + "\n- **Active Repositories**: " + repos.size() + "\n- **Pull Requests**: " + prs.size());
            return;
        }

        System.out.println();

        List<String> reportLines = new java.util.ArrayList<>();
        reportLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Commits")), AnsiStyle.boldWhite(String.valueOf(commits.size()))));
        reportLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Active Repositories")), AnsiStyle.boldCyan(String.valueOf(repos.size()))));
        reportLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Pull Requests")), AnsiStyle.boldYellow(String.valueOf(prs.size()))));
        reportLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Languages")), AnsiStyle.boldMagenta(String.valueOf(languages.size()))));

        BoxRenderer.renderBox("DEVSHELL " + period + " REPORT", reportLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
