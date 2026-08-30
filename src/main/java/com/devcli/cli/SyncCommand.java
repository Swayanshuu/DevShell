package com.devcli.cli;

import com.devcli.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "sync", description = "Synchronize recent repositories, commits, and activity from GitHub", mixinStandardHelpOptions = true)
public class SyncCommand implements Runnable {

    private final SyncService syncService;
    private final AuthService authService;

    @Autowired
    public SyncCommand(SyncService syncService, AuthService authService) {
        this.syncService = syncService;
        this.authService = authService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }
        syncService.syncAll(false);
    }
}
