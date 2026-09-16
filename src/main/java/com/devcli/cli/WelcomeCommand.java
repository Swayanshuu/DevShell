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
            loggedOutLines.add("  2. " + AnsiStyle.boldYellow("devshell help") + "    " + AnsiStyle.gray("View interactive command guide & features"));
            loggedOutLines.add("  3. " + AnsiStyle.boldYellow("devshell status") + "  " + AnsiStyle.gray("View daily developer command center snapshot"));

            BoxRenderer.renderBox("DevShell Launchpad (v" + currentVer + ")", loggedOutLines, AnsiStyle.YELLOW);
            System.out.println();

            System.out.print(AnsiStyle.boldCyan("  Select option [1-3, or Press ENTER to exit] > "));
            try {
                Scanner scanner = new Scanner(System.in);
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim().toLowerCase();
                    System.out.println();
                    if ("1".equals(input) || "login".equals(input)) {
                        System.out.println("  " + AnsiStyle.boldYellow("To login, run: ") + AnsiStyle.boldCyan("devshell login <your-github-token>"));
                        System.out.println(AnsiStyle.dim("\n  Press ENTER to exit..."));
                        scanner.nextLine();
                    } else if ("2".equals(input) || "help".equals(input)) {
                        helpCommand.run();
                        System.out.println(AnsiStyle.dim("\n  Press ENTER to exit..."));
                        scanner.nextLine();
                    } else if ("3".equals(input) || "status".equals(input)) {
                        statusCommand.run();
                        System.out.println(AnsiStyle.dim("\n  Press ENTER to exit..."));
                        scanner.nextLine();
                    }
                }
            } catch (Exception ignored) {}
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
        cardLines.add("");
        cardLines.add(AnsiStyle.boldCyan("INTERACTIVE QUICK COMMAND LAUNCHER"));
        cardLines.add(AnsiStyle.dim("─────────────────────────────────────────────────────────────"));
        cardLines.add("  " + AnsiStyle.boldYellow("[1]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell status")) + " " + AnsiStyle.gray("Daily developer command center snapshot"));
        cardLines.add("  " + AnsiStyle.boldYellow("[2]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell dna")) + " " + AnsiStyle.gray("Developer stack specialization & identity"));
        cardLines.add("  " + AnsiStyle.boldYellow("[3]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell trends")) + " " + AnsiStyle.gray("Historical commit & PR velocity trends"));
        cardLines.add("  " + AnsiStyle.boldYellow("[4]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell projects")) + " " + AnsiStyle.gray("Repository universe & activity health"));
        cardLines.add("  " + AnsiStyle.boldYellow("[5]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell learn")) + " " + AnsiStyle.gray("Terminal Developer Knowledge Base & journal"));
        cardLines.add("  " + AnsiStyle.boldYellow("[6]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell goals")) + " " + AnsiStyle.gray("Track developer goals & visual progress bars"));
        cardLines.add("  " + AnsiStyle.boldYellow("[7]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell xp")) + " " + AnsiStyle.gray("Developer level & XP progression breakdown"));
        cardLines.add("  " + AnsiStyle.boldYellow("[8]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell radar")) + " " + AnsiStyle.gray("Attention tracker for inactive repos & issues"));
        cardLines.add("  " + AnsiStyle.boldYellow("[9]") + " " + AnsiStyle.boldCyan(String.format("%-18s", "devshell help")) + " " + AnsiStyle.gray("View full interactive command guide"));
        cardLines.add("  " + AnsiStyle.boldYellow("[q]") + " " + AnsiStyle.boldRed(String.format("%-18s", "exit launcher")) + " " + AnsiStyle.gray("Close dashboard launcher"));

        BoxRenderer.renderBox("DevShell Dashboard", cardLines, AnsiStyle.CYAN);
        System.out.println();

        System.out.print(AnsiStyle.boldCyan("  Select command to launch [1-9, q to exit] > "));
        try {
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim().toLowerCase();
                System.out.println();
                switch (input) {
                    case "1":
                    case "status":
                        statusCommand.run();
                        break;
                    case "2":
                    case "dna":
                    case "profile":
                        dnaCommand.run();
                        break;
                    case "3":
                    case "trends":
                        trendsCommand.run();
                        break;
                    case "4":
                    case "projects":
                        projectsCommand.run();
                        break;
                    case "5":
                    case "learn":
                    case "kb":
                    case "journal":
                        learnCommand.run();
                        break;
                    case "6":
                    case "goals":
                    case "goal":
                        goalCommand.run();
                        break;
                    case "7":
                    case "xp":
                        xpCommand.run();
                        break;
                    case "8":
                    case "radar":
                        radarCommand.run();
                        break;
                    case "9":
                    case "help":
                        helpCommand.run();
                        break;
                    case "q":
                    case "exit":
                    case "quit":
                        System.out.println(AnsiStyle.dim("  Exiting DevShell dashboard.\n"));
                        break;
                    default:
                        System.out.println(AnsiStyle.dim("  Executing default status snapshot...\n"));
                        statusCommand.run();
                        break;
                }
            }
        } catch (Exception ignored) {
            System.out.println();
        }
    }
}
