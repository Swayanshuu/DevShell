package com.devcli.cli;

import com.devcli.model.Learning;
import com.devcli.model.Repository;
import com.devcli.service.AuthService;
import com.devcli.service.JournalService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;

@Component
@Command(name = "graph", description = "Represent visual relationships between Projects -> Technologies -> Learnings -> Activity", mixinStandardHelpOptions = true)
public class GraphCommand implements Runnable {

    private final LocalStorageService storageService;
    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public GraphCommand(LocalStorageService storageService, JournalService journalService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.journalService = journalService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Repository> repos = storageService.getRepositories();
        List<Learning> learnings = journalService.getLearnings();

        System.out.println();

        List<String> graphLines = new java.util.ArrayList<>();

        if (repos.isEmpty()) {
            graphLines.add("  " + AnsiStyle.dim("No repositories found in local database."));
            graphLines.add("  " + AnsiStyle.gray("Run devshell sync to pull your repositories from GitHub."));
        } else {
            int displayCount = Math.min(4, repos.size());
            for (int i = 0; i < displayCount; i++) {
                Repository repo = repos.get(i);
                graphLines.add("  " + AnsiStyle.boldCyan(String.format("%-22s", repo.getName().toUpperCase())));
                Map<String, Long> langs = repo.getLanguages();
                if (langs != null && !langs.isEmpty()) {
                    int lIdx = 0;
                    int totalLangs = langs.size();
                    for (String lang : langs.keySet()) {
                        if (lIdx >= 3) break;
                        boolean isLastLang = (lIdx == totalLangs - 1 || lIdx == 2);
                        String langPrefix = isLastLang ? "     └── " : "     ├── ";
                        graphLines.add(langPrefix + AnsiStyle.boldYellow(lang));
                        lIdx++;
                    }
                } else if (repo.getLanguage() != null && !repo.getLanguage().isEmpty()) {
                    graphLines.add("     └── " + AnsiStyle.boldYellow(repo.getLanguage()));
                } else {
                    graphLines.add("     └── " + AnsiStyle.dim("General Stack"));
                }
            }

            if (!learnings.isEmpty()) {
                graphLines.add("");
                graphLines.add("  " + AnsiStyle.boldMagenta("LEARNING KNOWLEDGE BASE"));
                int maxL = Math.min(3, learnings.size());
                for (int j = 0; j < maxL; j++) {
                    Learning l = learnings.get(j);
                    graphLines.add("     └── " + AnsiStyle.cyan("[" + l.getCategory() + "] ") + AnsiStyle.brightWhite(l.getTitle()));
                }
            }
        }

        BoxRenderer.renderBox("DEVELOPER DEPENDENCY & STACK GRAPH", graphLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
