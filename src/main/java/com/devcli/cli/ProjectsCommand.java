package com.devcli.cli;

import com.devcli.model.*;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import com.devcli.ui.TableRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Command(name = "projects", aliases = {
        "project" }, description = "Show your project universe or inspect individual project health", mixinStandardHelpOptions = true)
public class ProjectsCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Optional name of project to inspect")
    private String projectName;

    private final LocalStorageService storageService;
    private final ProjectHealthEngine healthEngine;
    private final AnalysisEngine analysisEngine;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public ProjectsCommand(LocalStorageService storageService, ProjectHealthEngine healthEngine,
            AnalysisEngine analysisEngine, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.healthEngine = healthEngine;
        this.analysisEngine = analysisEngine;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();

        if (repos.isEmpty()) {
            System.out.println(
                    AnsiStyle.brightRed("✗ No repositories cached. Run `devshell login` to fetch your GitHub data."));
            return;
        }

        if (projectName != null && !projectName.isEmpty()) {
            inspectProjectHealth(projectName, repos, commits, prs);
            return;
        }

        System.out.println();

        List<String> projectLines = new ArrayList<>();
        for (Repository r : repos) {
            ProjectHealth health = healthEngine.evaluateHealth(r, commits, prs);
            String symbol = (health.getState() == ProjectHealth.State.ACTIVE
                    || health.getState() == ProjectHealth.State.MAINTAINED) ? AnsiStyle.boldGreen("●")
                            : AnsiStyle.dim("○");
            String stateStr = health.getState() == ProjectHealth.State.ACTIVE ? AnsiStyle.boldGreen("ACTIVE")
                    : health.getState() == ProjectHealth.State.MAINTAINED ? AnsiStyle.boldYellow("MAINTAINED")
                            : health.getState() == ProjectHealth.State.LOW_ACTIVITY ? AnsiStyle.cyan("LOW ACTIVITY")
                                    : AnsiStyle.dim("DORMANT");

            projectLines.add(String.format("  %s %s   %s", symbol, AnsiStyle.boldWhite(String.format("%-22s", r.getName())), stateStr));
        }

        BoxRenderer.renderBox("PROJECT UNIVERSE (" + repos.size() + ")", projectLines, AnsiStyle.CYAN);
        System.out.println("  " + AnsiStyle.dim("Type `devshell project <name>` to inspect detailed project health.\n"));
    }

    private void inspectProjectHealth(String targetName, List<Repository> repos, List<Commit> commits,
            List<PullRequest> prs) {
        Optional<Repository> match = repos.stream()
                .filter(r -> r.getName().equalsIgnoreCase(targetName) || r.getFullName().equalsIgnoreCase(targetName))
                .findFirst();

        if (match.isEmpty()) {
            System.out
                    .println(AnsiStyle.brightRed("✗ Project '" + targetName + "' not found in your repository list."));
            return;
        }

        Repository repo = match.get();
        ProjectHealth health = healthEngine.evaluateHealth(repo, commits, prs);

        System.out.println();
        List<String> healthLines = new ArrayList<>();
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Commits")), AnsiStyle.boldWhite(String.valueOf(health.getCommitCount()))));
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Contributors")), AnsiStyle.boldCyan(String.valueOf(health.getContributorCount()))));
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Open Issues")), AnsiStyle.boldYellow(String.valueOf(health.getOpenIssuesCount()))));
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Pull Requests")), AnsiStyle.boldMagenta(String.valueOf(health.getPullRequestCount()))));
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Last Activity")), AnsiStyle.gray(analysisEngine.getRelativeTime(health.getLastActivity()))));
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Primary Language")), AnsiStyle.boldCyan(repo.getLanguage() != null ? repo.getLanguage() : "General")));
        healthLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Health State")), AnsiStyle.boldGreen(health.getState().name())));

        BoxRenderer.renderBox("HEALTH ANALYSIS: " + repo.getName().toUpperCase(), healthLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
