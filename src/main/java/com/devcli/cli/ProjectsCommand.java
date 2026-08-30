package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.Repository;
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
@Command(name = "projects", aliases = {"project"}, description = "Show your project universe or inspect an individual project", mixinStandardHelpOptions = true)
public class ProjectsCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Optional name of project to inspect")
    private String projectName;

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public ProjectsCommand(LocalStorageService storageService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
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

        if (repos.isEmpty()) {
            System.out.println(AnsiStyle.brightRed("✗ No repositories cached. Run `devcli login` to fetch your GitHub data."));
            return;
        }

        if (projectName != null && !projectName.isEmpty()) {
            inspectSingleProject(projectName, repos, commits);
            return;
        }

        BoxRenderer.printBanner("PROJECT UNIVERSE", repos.size() + " repositories categorized by activity");

        List<String> headers = List.of("Status", "Repository Name", "Primary Language", "Stars", "Forks", "Last Updated");
        List<List<String>> rows = new ArrayList<>();

        for (Repository r : repos) {
            String statusLabel = r.getStatus() != null ? r.getStatus().getLabel() : Repository.Status.INACTIVE.getLabel();
            String nameFormatted = r.isPrivate() ? AnsiStyle.boldCyan(r.getName()) + AnsiStyle.dim(" (private)") : AnsiStyle.boldCyan(r.getName());
            String lang = AnsiStyle.yellow(r.getLanguage());
            String stars = AnsiStyle.dim(r.getStars() + " ★");
            String forks = AnsiStyle.dim(r.getForks() + " ⑂");
            String updated = r.getUpdatedAt() != null ? r.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Recently";

            rows.add(List.of(statusLabel, nameFormatted, lang, stars, forks, updated));
        }

        TableRenderer.printTable(headers, rows);
        System.out.println("\n  " + AnsiStyle.dim("Tip: Type `devcli project <name>` to inspect a specific repository in detail.\n"));
    }

    private void inspectSingleProject(String targetName, List<Repository> repos, List<Commit> commits) {
        Optional<Repository> match = repos.stream()
                .filter(r -> r.getName().equalsIgnoreCase(targetName) || r.getFullName().equalsIgnoreCase(targetName))
                .findFirst();

        if (match.isEmpty()) {
            System.out.println(AnsiStyle.brightRed("✗ Project '" + targetName + "' not found in your repository list."));
            return;
        }

        Repository repo = match.get();
        BoxRenderer.printBanner("PROJECT: " + repo.getName(), repo.getDescription());

        System.out.println("  Full Name   : " + AnsiStyle.boldCyan(repo.getFullName()));
        System.out.println("  Status      : " + repo.getStatus().getLabel());
        System.out.println("  Visibility  : " + (repo.isPrivate() ? AnsiStyle.yellow("Private") : AnsiStyle.green("Public")));
        System.out.println("  Language    : " + AnsiStyle.boldYellow(repo.getLanguage()));
        System.out.println("  Stars / Forks: " + AnsiStyle.brightCyan(repo.getStars() + " ★") + " / " + AnsiStyle.cyan(repo.getForks() + " ⑂"));
        System.out.println("  Branch      : " + AnsiStyle.dim(repo.getDefaultBranch() != null ? repo.getDefaultBranch() : "main"));
        System.out.println("  Last Update : " + AnsiStyle.gray(repo.getUpdatedAt() != null ? repo.getUpdatedAt().toString() : "Recently"));

        BoxRenderer.printSectionHeader("RECENT COMMITS FOR " + repo.getName().toUpperCase());
        List<Commit> repoCommits = commits.stream()
                .filter(c -> c.getRepoName().equalsIgnoreCase(repo.getName()))
                .collect(Collectors.toList());

        if (repoCommits.isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("No recent commits cached for this repository."));
        } else {
            for (Commit c : repoCommits) {
                String date = c.getDate() != null ? c.getDate().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "Recently";
                System.out.println("  " + AnsiStyle.dim(date) + "  " + AnsiStyle.cyan("[" + c.getShortSha() + "]") + "  " + AnsiStyle.boldWhite(c.getShortMessage()));
            }
        }
        System.out.println();
    }
}
