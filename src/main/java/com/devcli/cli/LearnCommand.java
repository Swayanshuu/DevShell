package com.devcli.cli;

import com.devcli.model.Learning;
import com.devcli.service.*;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Command(name = "learn", description = "Save coding notes & discoveries with category (e.g. devshell learn \"your note\" Database)", mixinStandardHelpOptions = true)
public class LearnCommand implements Runnable {

    @Option(names = {"-c", "--category"}, description = "Specify category (e.g., Backend, Database, CLI, DevOps)")
    private String explicitCategory;

    @Parameters(arity = "0..*", description = "Learning title/note and optional category")
    private String[] inputParts;

    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public LearnCommand(JournalService journalService, AuthService authService, SyncService syncService) {
        this.journalService = journalService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        if (inputParts != null && inputParts.length > 0) {
            CategoryAndTitle result = parseCategoryAndTitle(inputParts, explicitCategory);
            if (result.title != null && !result.title.isEmpty()) {
                journalService.addLearning(result.title, result.category, "Recorded via DevCLI");
                System.out.println(AnsiStyle.boldGreen("\n✓ Recorded [" + result.category + "] learning: ") + AnsiStyle.brightWhite(result.title));
                System.out.println(AnsiStyle.dim("Keep expanding your knowledge base! 🧠\n"));
                return;
            }
        }

        List<Learning> learnings = journalService.getLearnings();
        BoxRenderer.printBanner("DEVELOPER JOURNAL - LEARNINGS 🧠", learnings.size() + " discoveries recorded");

        if (learnings.isEmpty()) {
            System.out.println("  " + AnsiStyle.dim("No learnings recorded yet. Run `devshell learn \"Your note here\" \"Category\"` to add one!"));
            System.out.println();
            return;
        }

        for (Learning l : learnings) {
            String dateStr = l.getCreatedAt() != null ? l.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Recently";
            System.out.println("  " + AnsiStyle.boldMagenta("🧠 " + l.getTitle()));
            System.out.println("     " + AnsiStyle.dim("Category: " + l.getCategory() + " • Logged: " + dateStr));
            if (l.getDescription() != null && !l.getDescription().isEmpty()) {
                System.out.println("     " + AnsiStyle.gray(l.getDescription()));
            }
            System.out.println();
        }
        System.out.println("  " + AnsiStyle.dim("Tip: Save a note anytime using `devshell learn \"<note>\" \"<category>\"`\n"));
    }

    private static class CategoryAndTitle {
        final String title;
        final String category;
        CategoryAndTitle(String title, String category) {
            this.title = title;
            this.category = category;
        }
    }

    private CategoryAndTitle parseCategoryAndTitle(String[] parts, String explicitCat) {
        if (explicitCat != null && !explicitCat.trim().isEmpty()) {
            String title = String.join(" ", parts).trim();
            return new CategoryAndTitle(title, explicitCat.trim());
        }

        if (parts.length >= 2) {
            String lastWord = parts[parts.length - 1].trim();
            if (isKnownCategory(lastWord)) {
                String[] titleParts = java.util.Arrays.copyOf(parts, parts.length - 1);
                String title = String.join(" ", titleParts).trim();
                return new CategoryAndTitle(title, capitalize(lastWord));
            }
        }

        String fullTitle = String.join(" ", parts).trim();
        String autoCategory = autoDetectCategory(fullTitle);
        return new CategoryAndTitle(fullTitle, autoCategory);
    }

    private boolean isKnownCategory(String word) {
        if (word == null || word.isEmpty()) return false;
        String w = word.toLowerCase();
        return w.equals("backend") || w.equals("frontend") || w.equals("cli") || w.equals("database")
                || w.equals("devops") || w.equals("security") || w.equals("ai") || w.equals("general")
                || w.equals("testing") || w.equals("architecture") || w.equals("mobile");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String autoDetectCategory(String title) {
        if (title == null) return "General";
        String lower = title.toLowerCase();
        if (lower.contains("cli") || lower.contains("picocli") || lower.contains("terminal") || lower.contains("cmd") || lower.contains("shell")) {
            return "CLI";
        }
        if (lower.contains("sql") || lower.contains("redis") || lower.contains("db") || lower.contains("postgres") || lower.contains("mongo")) {
            return "Database";
        }
        if (lower.contains("docker") || lower.contains("k8s") || lower.contains("git") || lower.contains("devops") || lower.contains("deploy") || lower.contains("npm")) {
            return "DevOps";
        }
        if (lower.contains("auth") || lower.contains("token") || lower.contains("jwt") || lower.contains("security") || lower.contains("oauth")) {
            return "Security";
        }
        if (lower.contains("react") || lower.contains("ui") || lower.contains("css") || lower.contains("html") || lower.contains("frontend") || lower.contains("vue")) {
            return "Frontend";
        }
        if (lower.contains("spring") || lower.contains("backend") || lower.contains("api") || lower.contains("webclient") || lower.contains("service") || lower.contains("java") || lower.contains("node")) {
            return "Backend";
        }
        return "General";
    }
}
