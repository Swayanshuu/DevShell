package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.model.PullRequest;
import com.devcli.model.Repository;
import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "opensource", description = "Track open-source journey, contributions, and community impact", mixinStandardHelpOptions = true)
public class OpenSourceCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public OpenSourceCommand(LocalStorageService storageService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();

        long publicRepos = repos.stream().filter(r -> !r.isPrivate()).count();
        long openPrs = prs.stream().filter(p -> "OPEN".equalsIgnoreCase(p.getState())).count();
        long mergedPrs = prs.stream().filter(p -> "MERGED".equalsIgnoreCase(p.getState()) || "CLOSED".equalsIgnoreCase(p.getState())).count();
        int bugCount = storageService.getBugs() != null ? storageService.getBugs().size() : 0;

        System.out.println();
        List<String> osLines = new java.util.ArrayList<>();
        osLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Public Repositories")), AnsiStyle.boldWhite(String.valueOf(publicRepos))));
        osLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Total Pull Requests")), AnsiStyle.boldYellow(String.valueOf(prs.size()))));
        osLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Merged Pull Requests")), AnsiStyle.boldGreen(String.valueOf(mergedPrs))));
        osLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Open Pull Requests")), AnsiStyle.boldCyan(String.valueOf(openPrs))));
        osLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Tracked Issues / Bugs")), AnsiStyle.boldMagenta(String.valueOf(bugCount))));
        osLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-25s", "Total Contributions")), AnsiStyle.boldGreen(String.format("%,d", commits.size()))));

        BoxRenderer.renderBox("OPEN SOURCE & COMMUNITY JOURNEY", osLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
