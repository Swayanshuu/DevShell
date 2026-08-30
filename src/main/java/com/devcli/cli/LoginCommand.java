package com.devcli.cli;

import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "login", description = "Authorize DevShell with your GitHub account", mixinStandardHelpOptions = true)
public class LoginCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Optional Personal Access Token (PAT)")
    private String token;

    private final AuthService authService;
    private final SyncService syncService;
    private final StatusCommand statusCommand;

    @Autowired
    public LoginCommand(AuthService authService, SyncService syncService, StatusCommand statusCommand) {
        this.authService = authService;
        this.syncService = syncService;
        this.statusCommand = statusCommand;
    }

    @Override
    public void run() {
        authService.login(token);
        if (authService.isLoggedIn()) {
            syncService.syncAll(false);
            statusCommand.run();
        }
    }
}
