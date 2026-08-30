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

        // 1. Primary Commands
        System.out.println("  " + AnsiStyle.boldCyan("🚀 PRIMARY COMMANDS"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("status", "Show your Personal Developer Command Center snapshot (default)");
        printCmd("login", "Authorize DevShell with your GitHub account (OAuth/PAT)");
        printCmd("logout", "Revoke stored credentials and clear local cache");
        System.out.println();

        // 2. Analytics & DNA
        System.out.println("  " + AnsiStyle.boldGreen("📊 ANALYTICS & DEVELOPER DNA"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("stats", "View your Developer DNA report & language stack breakdown");
        printCmd("projects", "Show categorized project universe or inspect (`devshell project <name>`)");
        printCmd("activity", "Timeline feed of GitHub activity (`--today`, `--week`, `--project`)");
        printCmd("insight", "Smart data-driven observations about your development patterns");
        System.out.println();

        // 3. Gamification & Milestones
        System.out.println("  " + AnsiStyle.boldYellow("🏆 GAMIFICATION & MILESTONES"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("achievements", "View unlocked developer badges & progress trackers");
        printCmd("timeline", "Visual chronological milestone graph across recent weeks");
        System.out.println();

        // 4. Journal & Report Export
        System.out.println("  " + AnsiStyle.boldMagenta("📝 JOURNAL & REPORT EXPORT"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("learn", "Log or view developer discoveries (`devshell learn \"<title>\"`)");
        printCmd("bugs", "Track, log, and resolve local bugs (`--add`, `--resolve`)");
        printCmd("export", "Export Developer DNA report (`--format markdown|json|html`)");
        System.out.println();

        // 5. Utilities & Flags
        System.out.println("  " + AnsiStyle.boldCyan("⚙️ UTILITIES & FLAGS"));
        System.out.println("  " + AnsiStyle.dim("──────────────────────────────────────────────────────────────────"));
        printCmd("sync", "Synchronize recent repositories, commits, and activity from GitHub");
        printFlag("--debug", "Enable detailed technical debug output and trace logs");
        printFlag("-h, --help", "Show this kick-ass help screen");
        printFlag("-V, --version", "Print DevShell version information");
        System.out.println();

        // 6. Pro-Tip Examples
        System.out.println("  " + AnsiStyle.boldWhite("💡 QUICK EXAMPLES:"));
        System.out.println("    " + AnsiStyle.cyan("devshell status") + "                    # View daily developer snapshot");
        System.out.println("    " + AnsiStyle.cyan("devshell stats") + "                     # View Developer DNA report");
        System.out.println("    " + AnsiStyle.cyan("devshell project LinkPeer") + "          # Inspect specific repository");
        System.out.println("    " + AnsiStyle.cyan("devshell learn \"Reactive RxJava\"") + "   # Record a discovery");
        System.out.println("    " + AnsiStyle.cyan("devshell export --format markdown") + "  # Export developer report\n");
    }

    private static void printCmd(String cmd, String desc) {
        System.out.printf("    %-18s %s\n", AnsiStyle.boldCyan(cmd), AnsiStyle.gray(desc));
    }

    private static void printFlag(String flag, String desc) {
        System.out.printf("    %-18s %s\n", AnsiStyle.boldYellow(flag), AnsiStyle.dim(desc));
    }
}
