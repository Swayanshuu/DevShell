package com.devcli.cli;

import com.devcli.model.UserProfile;
import com.devcli.service.AuthService;
import com.devcli.service.SyncService;
import com.devcli.service.UpdateCheckerService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
@Command(name = "welcome", description = "Show interactive DevShell dashboard and command launcher", mixinStandardHelpOptions = true)
public class WelcomeCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AuthService authService;
    private final SyncService syncService;

    private final StatusCommand statusCommand;
    private final DnaCommand dnaCommand;
    private final TrendsCommand trendsCommand;
    private final ProjectsCommand projectsCommand;
    private final LearnCommand learnCommand;
    private final GoalCommand goalCommand;
    private final XpCommand xpCommand;
    private final RadarCommand radarCommand;
    private final HelpCommand helpCommand;

    @Autowired
    public WelcomeCommand(LocalStorageService storageService,
                          AuthService authService,
                          SyncService syncService,
                          StatusCommand statusCommand,
                          DnaCommand dnaCommand,
                          TrendsCommand trendsCommand,
                          ProjectsCommand projectsCommand,
                          LearnCommand learnCommand,
                          GoalCommand goalCommand,
                          XpCommand xpCommand,
                          RadarCommand radarCommand,
                          HelpCommand helpCommand) {
        this.storageService = storageService;
        this.authService = authService;
        this.syncService = syncService;
        this.statusCommand = statusCommand;
        this.dnaCommand = dnaCommand;
        this.trendsCommand = trendsCommand;
        this.projectsCommand = projectsCommand;
        this.learnCommand = learnCommand;
        this.goalCommand = goalCommand;
        this.xpCommand = xpCommand;
        this.radarCommand = radarCommand;
        this.helpCommand = helpCommand;
    }

    @Override
    public void run() {
        BoxRenderer.printAsciiBanner();

        String currentVer = UpdateCheckerService.getCurrentVersion();

        if (!authService.isLoggedIn()) {
            System.out.println("  " + AnsiStyle.boldYellow("AUTHENTICATION REQUIRED"));
            System.out.println("  " + AnsiStyle.boldWhite("Type ") + AnsiStyle.boldCyan("devshell login") + AnsiStyle.boldWhite(" to connect your GitHub account.\n"));

            List<String> loggedOutLines = new ArrayList<>();
            loggedOutLines.add(AnsiStyle.boldCyan("GETTING STARTED"));
            loggedOutLines.add(AnsiStyle.dim("─────────────────────────────────────────────────────────────"));
            loggedOutLines.add("  1. " + AnsiStyle.boldYellow("devshell login") + "   " + AnsiStyle.gray("Connect GitHub account & set up token"));
            loggedOutLines.add("  2. " + AnsiStyle.boldYellow("devshell help") + "    " + AnsiStyle.gray("View command guide & available features"));
            loggedOutLines.add("  3. " + AnsiStyle.boldYellow("devshell status") + "  " + AnsiStyle.gray("View daily developer command center snapshot"));

            BoxRenderer.renderBox("DevShell Launchpad (v" + currentVer + ")", loggedOutLines, AnsiStyle.YELLOW);
            System.out.println();
            return;
        }

        syncService.syncAll(true);

        UserProfile profile = storageService.getUserProfile();
        String username = (profile != null && profile.getUsername() != null && !profile.getUsername().isEmpty())
                ? profile.getUsername()
                : "Developer";

        String displayName = (profile != null && profile.getName() != null && !profile.getName().isEmpty())
                ? profile.getName()
                : username;

        System.out.println("  " + AnsiStyle.boldCyan("Welcome back, ") + AnsiStyle.boldYellow("@" + username) + AnsiStyle.boldCyan(" (" + displayName + ")"));
        System.out.println("  " + AnsiStyle.boldWhite("DevShell Personal Developer Operating System (v" + currentVer + ")."));

        if (profile != null && profile.getBio() != null && !profile.getBio().isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("Bio: ") + AnsiStyle.italic(profile.getBio()));
        }
        System.out.println();

        List<String> cardLines = new ArrayList<>();
        cardLines.add(AnsiStyle.boldCyan("DEVELOPER PROFILE DETAILS"));
        cardLines.add(AnsiStyle.dim("─────────────────────────────────────────────────────────────"));
        cardLines.add(String.format("  %-18s %s", AnsiStyle.boldWhite("GitHub Handle:"), AnsiStyle.boldYellow("@" + username)));
        if (profile != null) {
            cardLines.add(String.format("  %-18s %s", AnsiStyle.boldWhite("Public Repos:"), AnsiStyle.cyan(profile.getPublicRepos() + " repos")));
            cardLines.add(String.format("  %-18s %s", AnsiStyle.boldWhite("Social:"), AnsiStyle.gray(profile.getFollowers() + " followers • " + profile.getFollowing() + " following")));
            if (profile.getAuthType() != null) {
                cardLines.add(String.format("  %-18s %s", AnsiStyle.boldWhite("Auth Mode:"), AnsiStyle.green(profile.getAuthType() + " Verified")));
            }
        }
        cardLines.add(String.format("  %-18s %s", AnsiStyle.boldWhite("App Version:"), AnsiStyle.boldYellow("v" + currentVer)));

        BoxRenderer.renderBox("DevShell Dashboard", cardLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
