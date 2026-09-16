package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.CalendarRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "calendar", description = "Display terminal-based contribution calendar grid", mixinStandardHelpOptions = true)
public class CalendarCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public CalendarCommand(LocalStorageService storageService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) return;

        List<Commit> commits = storageService.getCommits();
        System.out.println();
        CalendarRenderer.renderContributionCalendar(commits);
    }
}
