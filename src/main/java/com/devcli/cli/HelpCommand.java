package com.devcli.cli;

import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "help", description = "Show kick-ass interactive help screen and command guide", mixinStandardHelpOptions = true)
public class HelpCommand implements Runnable {

    @Override
    public void run() {
        printHelpScreen();
    }

    public static void printHelpScreen() {
        BoxRenderer.printAsciiBanner();

        System.out.println("  " + AnsiStyle.boldWhite("USAGE:"));
        System.out.println("    " + AnsiStyle.boldCyan("devshell") + " " + AnsiStyle.yellow("[COMMAND]") + " " + AnsiStyle.dim("[OPTIONS]"));
        System.out.println("    " + AnsiStyle.dim("(Alias: devcli [COMMAND] [OPTIONS])"));
        System.out.println();

        // 1. Core & Dashboard
        System.out.println("  " + AnsiStyle.boldCyan("🚀 1. CORE & DASHBOARD"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("status", "Show daily developer cockpit snapshot, active branch & goals");
        printCmd("welcome", "Launch interactive terminal dashboard & onboarding");
        printCmd("login", "Authorize DevShell with GitHub account (OAuth / Personal Access Token)");
        printCmd("logout", "Revoke stored credentials and clear local cache");
        printCmd("sync", "Synchronize repositories, commits, and activity from GitHub");
        System.out.println();

        // 2. Intelligence & Analytics
        System.out.println("  " + AnsiStyle.boldGreen("📊 2. INTELLIGENCE & ANALYTICS"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("dna", "Developer DNA profile, stack breakdown & archetype matrix");
        printCmd("trends", "Predictive velocity trends, commit acceleration & streaks");
        printCmd("stats", "Codebase stats, language distribution & telemetry breakdown");
        printCmd("radar", "Repository health matrix, code rot & risk score detector");
        printCmd("diff", "Compare git activity between branches, dates, or repositories");
        printCmd("graph", "Visual ASCII branch & commit dependency topology graph");
        printCmd("insight", "Smart data-driven observations on coding habits");
        System.out.println();

        // 3. Gamification & Productivity
        System.out.println("  " + AnsiStyle.boldYellow("🏆 3. GAMIFICATION & PRODUCTIVITY"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("xp", "Level up engine, rank progression & streak bonus multipliers");
        printCmd("achievements", "View unlocked developer badges & milestone trackers");
        printCmd("goals", "Set, view, and track daily & weekly developer goals (alias: goal)");
        printCmd("focus", "Focus timer, Pomodoro sessions & distraction-free coding tracker");
        printCmd("snapshot", "Save & restore workspace state, open files & environment context");
        System.out.println();

        // 4. Knowledge & Project Management
        System.out.println("  " + AnsiStyle.boldMagenta("📝 4. KNOWLEDGE & PROJECT MANAGEMENT"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("projects", "Show categorized project universe or inspect (`devshell project <name>`)");
        printCmd("activity", "Timeline feed of GitHub & local git activity (`--today`, `--week`)");
        printCmd("timeline", "Chronological milestone graph across recent weeks");
        printCmd("learn", "Save coding notes (`devshell learn \"<note>\" \"<category>\"`)");
        printCmd("bugs", "Track, log, and resolve local bugs & issues (`--add`, `--resolve`)");
        printCmd("history", "Local execution log & CLI command analytics");
        System.out.println();

        // 5. Exporters & Showcase
        System.out.println("  " + AnsiStyle.boldBlue("🎁 5. EXPORT & SHOWCASE"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("wrapped", "Generate annual/monthly developer wrapped recap summary");
        printCmd("portfolio", "Generate dynamic, responsive HTML developer portfolio site");
        printCmd("report", "Generate detailed executive PDF/HTML status report");
        printCmd("export", "Multi-format exporter (`devshell export --format markdown|json|html`)");
        printCmd("prompt", "AI context builder for LLM coding prompts & shell integration");
        printCmd("opensource", "Track open-source contributions, PRs, and community stats");
        System.out.println();

        // 6. Global Flags
        System.out.println("  " + AnsiStyle.boldCyan("⚙️ 6. UTILITIES & GLOBAL FLAGS"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printFlag("help", "Display this kick-ass interactive command guide");
        printFlag("--json", "Emit pure machine-readable JSON output");
        printFlag("--no-color", "Disable ANSI color formatting");
        printFlag("--quiet", "Suppress non-essential console output");
        printFlag("--debug", "Enable detailed technical debug output and trace logs");
        printFlag("-V, --version", "Print DevShell version information");
        System.out.println();

        // Quick Examples
        System.out.println("  " + AnsiStyle.boldWhite("💡 KICK-ASS EXAMPLES:"));
        System.out.println("    " + AnsiStyle.cyan("devshell status") + "                    # View daily developer snapshot");
        System.out.println("    " + AnsiStyle.cyan("devshell dna") + "                       # View Developer DNA & archetype");
        System.out.println("    " + AnsiStyle.cyan("devshell wrapped") + "                   # Generate developer wrapped summary");
        System.out.println("    " + AnsiStyle.cyan("devshell portfolio --open") + "          # Build and open HTML portfolio in browser");
        System.out.println("    " + AnsiStyle.cyan("devshell learn \"Redis Pub/Sub\" Database") + " # Save note with category");
        System.out.println("    " + AnsiStyle.cyan("devshell export --format markdown") + "  # Export developer report\n");
    }

    private static void printCmd(String cmd, String desc) {
        System.out.printf("    %-18s %s\n", AnsiStyle.boldCyan(cmd), AnsiStyle.gray(desc));
    }

    private static void printFlag(String flag, String desc) {
        System.out.printf("    %-18s %s\n", AnsiStyle.boldYellow(flag), AnsiStyle.dim(desc));
    }
}
