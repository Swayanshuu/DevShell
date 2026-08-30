package com.devcli.service;

import com.devcli.model.UserProfile;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import com.devcli.ui.LoadingSpinner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.net.URI;
import java.util.Scanner;

@Service
public class AuthService {

    private final LocalStorageService storageService;
    private final GitHubService gitHubService;

    @Autowired
    public AuthService(LocalStorageService storageService, GitHubService gitHubService) {
        this.storageService = storageService;
        this.gitHubService = gitHubService;
    }

    public boolean isLoggedIn() {
        return storageService.hasUserProfile();
    }

    public boolean ensureAuthenticated(SyncService syncService) {
        if (!isLoggedIn()) {
            BoxRenderer.printAsciiBanner();
            System.out.println("  " + AnsiStyle.boldWhite("Welcome to DevShell 👋"));
            System.out.println("  " + AnsiStyle.dim("Let's connect your GitHub account to start tracking your developer DNA.\n"));

            login(null);
            return isLoggedIn();
        }
        return true;
    }

    public UserProfile getCurrentUser() {
        return storageService.getUserProfile();
    }

    public void login(String tokenArg) {
        BoxRenderer.printBanner("DEVSHELL GITHUB LOGIN", "Connecting your GitHub account to DevShell");

        String token = tokenArg;

        if (token == null || token.trim().isEmpty()) {
            String authUrl = "https://github.com/settings/tokens/new?scopes=repo,read:user,user:email&description=DevShell+Command+Center";
            
            System.out.println("  " + AnsiStyle.boldCyan("🔒 GITHUB AUTHORIZATION SETUP"));
            System.out.println("  " + AnsiStyle.dim("─────────────────────────────────────────────────────────────"));
            System.out.println("  Follow these 4 simple steps to connect your GitHub account:\n");

            System.out.println("  " + AnsiStyle.boldYellow("Step 1:") + " Open GitHub Token Page in your browser:");
            System.out.println("          👉 " + AnsiStyle.brightBlue(authUrl) + "\n");

            System.out.println("  " + AnsiStyle.boldYellow("Step 2:") + " Ensure these recommended scopes are checked:");
            System.out.println("          • " + AnsiStyle.cyan("[x] repo") + "         (Access repositories & commits)");
            System.out.println("          • " + AnsiStyle.cyan("[x] read:user") + "    (Access profile & streak analytics)");
            System.out.println("          • " + AnsiStyle.cyan("[x] user:email") + "   (Access contribution matching)\n");

            System.out.println("  " + AnsiStyle.boldYellow("Step 3:") + " Scroll to the bottom and click " + AnsiStyle.boldGreen("\"Generate token\"") + ".\n");

            System.out.println("  " + AnsiStyle.boldYellow("Step 4:") + " Copy your new token (starts with " + AnsiStyle.cyan("ghp_") + " or " + AnsiStyle.cyan("github_pat_") + ").\n");
            System.out.println("  " + AnsiStyle.dim("─────────────────────────────────────────────────────────────"));

            // Attempt to open browser automatically
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    System.out.println("  " + AnsiStyle.dim("Opening browser to GitHub token generator..."));
                    Desktop.getDesktop().browse(new URI(authUrl));
                }
            } catch (Exception ignored) {}

            System.out.print(AnsiStyle.boldCyan("\n  Paste your GitHub Access Token here: "));
            Scanner scanner = new Scanner(System.in);
            token = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        }

        if (token == null || token.trim().isEmpty()) {
            System.out.println(AnsiStyle.brightRed("\n✗ No token provided. Authentication cancelled."));
            return;
        }

        try {
            LoadingSpinner spinner = LoadingSpinner.start("Validating token with GitHub API...");
            UserProfile profile = gitHubService.fetchUserProfile(token);
            storageService.saveUserProfile(profile);
            spinner.stopSuccess("Authorization successful for @" + profile.getUsername() + "!");

            System.out.println("\n  " + AnsiStyle.boldGreen("✓ Welcome aboard, @" + profile.getUsername() + "! 🎉"));
            System.out.println("  " + AnsiStyle.dim("Credentials saved locally at ~/.devshell/profile.json"));
            System.out.println("  " + AnsiStyle.dim("Type `devshell status` to view your dashboard.\n"));
        } catch (Exception e) {
            System.out.println(AnsiStyle.brightRed("\n✗ Authentication failed: " + e.getMessage()));
            System.out.println(AnsiStyle.yellow("Please double check your token and run `devshell login` to try again.\n"));
        }
    }

    public void logout() {
        if (!isLoggedIn()) {
            System.out.println(AnsiStyle.yellow("You are not currently logged in."));
            return;
        }
        UserProfile user = getCurrentUser();
        storageService.clearData();
        System.out.println(AnsiStyle.boldGreen("✓ Logged out @" + (user != null ? user.getUsername() : "") + ". Saved credentials and local cache cleared."));
    }
}
