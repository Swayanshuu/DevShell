package com.devcli.cli;

import com.devcli.model.UserProfile;
import com.devcli.service.AuthService;
import com.devcli.service.UpdateCheckerService;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;

@Component
@Command(name = "welcome", description = "Show interactive welcome screen", mixinStandardHelpOptions = true)
public class WelcomeCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AuthService authService;

    @Autowired
    public WelcomeCommand(LocalStorageService storageService, AuthService authService) {
        this.storageService = storageService;
        this.authService = authService;
    }

    @Override
    public void run() {
        BoxRenderer.printAsciiBanner();

        String currentVer = UpdateCheckerService.getCurrentVersion();

        if (!authService.isLoggedIn()) {
            System.out.println("  " + AnsiStyle.boldYellow("🔒 Ohh, I see you are not logged in!"));
            System.out.println("  " + AnsiStyle.boldWhite("Type ") + AnsiStyle.boldCyan("devshell login") + AnsiStyle.boldWhite(" to log in yourself and connect your GitHub account.\n"));

            List<String> loggedOutLines = new ArrayList<>();
            loggedOutLines.add(AnsiStyle.boldCyan("⚡ GETTING STARTED"));
            loggedOutLines.add(AnsiStyle.dim("───────────────────────────────────────────────────"));
            loggedOutLines.add("  1. " + AnsiStyle.boldYellow("devshell login") + "   " + AnsiStyle.gray("Connect GitHub account & set up token"));
            loggedOutLines.add("  2. " + AnsiStyle.boldYellow("devshell help") + "    " + AnsiStyle.gray("View interactive command guide & features"));
            loggedOutLines.add("  3. " + AnsiStyle.boldYellow("devshell") + "         " + AnsiStyle.gray("Run devshell anytime to view your interactive dashboard"));

            BoxRenderer.renderBox("🚀 DevShell Launchpad (v" + currentVer + ")", loggedOutLines, AnsiStyle.YELLOW);
            System.out.println();
            return;
        }

        UserProfile profile = storageService.getUserProfile();
        String username = (profile != null && profile.getUsername() != null && !profile.getUsername().isEmpty())
                ? profile.getUsername()
                : "Developer";

        String displayName = (profile != null && profile.getName() != null && !profile.getName().isEmpty())
                ? profile.getName()
                : username;

        System.out.println("  " + AnsiStyle.boldCyan("👋 Welcome back, ") + AnsiStyle.boldYellow("@" + username) + AnsiStyle.boldCyan(" (" + displayName + ")!"));
        System.out.println("  " + AnsiStyle.boldWhite("I am DevShell — your personal developer command center (v" + currentVer + ")."));

        if (profile != null && profile.getBio() != null && !profile.getBio().isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("  Bio: ") + AnsiStyle.italic(profile.getBio()));
        }
        System.out.println("  " + AnsiStyle.gray("Ready to boost your productivity. What would you like to explore today?"));
        System.out.println();

        List<String> cardLines = new ArrayList<>();
        cardLines.add(AnsiStyle.boldCyan("👤 DEVELOPER PROFILE DETAILS"));
        cardLines.add(AnsiStyle.dim("───────────────────────────────────────────────────"));
        cardLines.add("  • " + AnsiStyle.boldWhite("GitHub Handle: ") + AnsiStyle.boldYellow("@" + username));
        if (profile != null) {
            cardLines.add("  • " + AnsiStyle.boldWhite("Public Repos:  ") + AnsiStyle.cyan(String.valueOf(profile.getPublicRepos())));
            cardLines.add("  • " + AnsiStyle.boldWhite("Social:        ") + AnsiStyle.gray(profile.getFollowers() + " followers • " + profile.getFollowing() + " following"));
            if (profile.getAuthType() != null) {
                cardLines.add("  • " + AnsiStyle.boldWhite("Auth Mode:     ") + AnsiStyle.green(profile.getAuthType()));
            }
        }
        cardLines.add("  • " + AnsiStyle.boldWhite("App Version:   ") + AnsiStyle.boldYellow("v" + currentVer));
        cardLines.add("");
        cardLines.add(AnsiStyle.boldCyan("⚡ QUICK COMMAND LAUNCHER"));
        cardLines.add(AnsiStyle.dim("───────────────────────────────────────────────────"));
        cardLines.add("  • " + AnsiStyle.boldYellow("devshell status") + "      " + AnsiStyle.gray("View developer command center snapshot"));
        cardLines.add("  • " + AnsiStyle.boldYellow("devshell activity") + "    " + AnsiStyle.gray("Stream recent GitHub commits & events"));
        cardLines.add("  • " + AnsiStyle.boldYellow("devshell stats") + "       " + AnsiStyle.gray("Inspect Developer DNA & tech stack"));
        cardLines.add("  • " + AnsiStyle.boldYellow("devshell projects") + "    " + AnsiStyle.gray("Browse repository universe & metrics"));
        cardLines.add("  • " + AnsiStyle.boldYellow("devshell learn") + "       " + AnsiStyle.gray("Save personal coding notes & TIL discoveries"));
        cardLines.add("  • " + AnsiStyle.boldYellow("devshell help") + "        " + AnsiStyle.gray("View full interactive command guide"));

        BoxRenderer.renderBox("🚀 DevShell Dashboard", cardLines, AnsiStyle.CYAN);
        System.out.println();

        System.out.println("  " + AnsiStyle.boldYellow("💡 PRO TIP: ") + AnsiStyle.gray("Type ") + AnsiStyle.cyan("devshell <command>") + AnsiStyle.gray(" to execute any feature directly."));
        System.out.println();
    }
}
