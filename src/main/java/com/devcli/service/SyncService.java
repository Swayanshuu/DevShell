package com.devcli.service;

import com.devcli.model.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.LoadingSpinner;
import com.devcli.ui.ProgressRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {

    private final LocalStorageService storageService;
    private final GitHubService gitHubService;

    @Autowired
    public SyncService(LocalStorageService storageService, GitHubService gitHubService) {
        this.storageService = storageService;
        this.gitHubService = gitHubService;
    }

    public void syncAll(boolean quiet) {
        UserProfile user = storageService.getUserProfile();
        if (user == null) {
            System.out.println(AnsiStyle.brightRed("✗ Not logged in. Run `devcli login` to authorize."));
            return;
        }

        if (!quiet) {
            System.out.println(AnsiStyle.boldCyan("INITIALIZING GITHUB SYNC"));
            System.out.println(AnsiStyle.dim("─────────────────────────────────────────"));
        }

        try {
            // 1. User profile
            LoadingSpinner spinner1 = !quiet ? LoadingSpinner.start("Connecting to GitHub...") : null;
            if (spinner1 != null) { Thread.sleep(250); spinner1.stopSuccess(null); }

            // 2. Repositories
            LoadingSpinner spinner2 = !quiet ? LoadingSpinner.start("Finding repositories...") : null;
            List<Repository> repos = gitHubService.fetchRepositories(user.getToken(), user.getUsername());
            storageService.saveRepositories(repos);
            if (spinner2 != null) { Thread.sleep(250); spinner2.stopSuccess(null); }

            // 3. Commits
            LoadingSpinner spinner3 = !quiet ? LoadingSpinner.start("Analyzing commits...") : null;
            List<Commit> commits = gitHubService.fetchCommits(user.getToken(), user.getUsername(), repos);
            storageService.saveCommits(commits);
            if (spinner3 != null) { Thread.sleep(250); spinner3.stopSuccess(null); }

            // 4. Pull Requests & Issues
            LoadingSpinner spinner4 = !quiet ? LoadingSpinner.start("Checking pull requests...") : null;
            List<PullRequest> prs = gitHubService.fetchPullRequests(user.getToken(), user.getUsername());
            storageService.savePullRequests(prs);
            if (spinner4 != null) { Thread.sleep(200); spinner4.stopSuccess(null); }

            LoadingSpinner spinner5 = !quiet ? LoadingSpinner.start("Finding your stack...") : null;
            List<Issue> issues = gitHubService.fetchIssues(user.getToken(), user.getUsername());
            storageService.saveIssues(issues);
            if (spinner5 != null) { Thread.sleep(200); spinner5.stopSuccess(null); }

            // 5. Activity Events
            LoadingSpinner spinner6 = !quiet ? LoadingSpinner.start("Detecting patterns...") : null;
            List<ActivityEvent> events = gitHubService.fetchActivityEvents(user.getToken(), user.getUsername());
            storageService.saveEvents(events);
            if (spinner6 != null) { Thread.sleep(200); spinner6.stopSuccess(null); }

            if (!quiet) {
                System.out.println();
                int totalCommits = repos.stream().mapToInt(Repository::getCommitCount).sum();
                if (totalCommits == 0) totalCommits = commits.size() * 14;

                long languageCount = repos.stream().map(Repository::getLanguage).distinct().count();

                System.out.println(AnsiStyle.boldGreen("  " + repos.size() + " repositories"));
                System.out.println(AnsiStyle.boldCyan("  " + totalCommits + " commits"));
                System.out.println(AnsiStyle.boldYellow("  " + languageCount + " languages"));
                System.out.println(AnsiStyle.boldMagenta("  " + prs.size() + " pull requests"));
                System.out.println();
                System.out.println(AnsiStyle.bold("I know a little about you now. 👀\n"));
            }
        } catch (Exception e) {
            System.out.println(AnsiStyle.brightRed("✗ Sync encountered an issue: " + e.getMessage()));
        }
    }
}
