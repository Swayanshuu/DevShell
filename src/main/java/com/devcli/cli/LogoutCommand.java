package com.devcli.cli;

import com.devcli.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "logout", description = "Revoke stored credentials and clear local cache", mixinStandardHelpOptions = true)
public class LogoutCommand implements Runnable {

    private final AuthService authService;

    @Autowired
    public LogoutCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void run() {
        authService.logout();
    }
}
