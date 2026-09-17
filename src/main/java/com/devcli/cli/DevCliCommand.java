package com.devcli.cli;

import com.devcli.ui.AnsiStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Component
@Command(name = "devshell", aliases = {
        "devcli" }, description = "DevShell - Personal Developer Operating System & Terminal Analytics", subcommands = {
                StatusCommand.class,
                DnaCommand.class,
                TrendsCommand.class,
                DiffCommand.class,
                CalendarCommand.class,
                GoalCommand.class,
                XpCommand.class,
                RadarCommand.class,
                FocusCommand.class,
                SnapshotCommand.class,
                HistoryCommand.class,
                GraphCommand.class,
                OpenSourceCommand.class,
                WrappedCommand.class,
                PortfolioCommand.class,
                ReportCommand.class,
                PromptCommand.class,
                LoginCommand.class,
                LogoutCommand.class,
                StatsCommand.class,
                ProjectsCommand.class,
                ActivityCommand.class,
                AchievementsCommand.class,
                InsightCommand.class,
                LearnCommand.class,
                BugsCommand.class,
                TimelineCommand.class,
                SyncCommand.class,
                HelpCommand.class,
                ExportCommand.class,
                WelcomeCommand.class,
                WrapCommand.class,
        }, mixinStandardHelpOptions = true, versionProvider = DevCliCommand.VersionProvider.class)

public class DevCliCommand implements Callable<Integer> {

    public static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[] {
                    "DevShell " + com.devcli.service.UpdateCheckerService.getCurrentVersion() + " (Swynx)" };
        }
    }

    @Option(names = { "--json" }, description = "Emit pure machine-readable JSON output")
    private boolean json;

    @Option(names = { "--no-color" }, description = "Disable ANSI color formatting")
    private boolean noColor;

    @Option(names = { "--theme" }, description = "Color theme (devshell | white)")
    private String theme;

    @Option(names = { "--quiet" }, description = "Suppress non-essential console logs")
    private boolean quiet;

    @Option(names = { "--debug" }, description = "Enable detailed technical debug output and trace logs")
    private boolean debug;

    private final WelcomeCommand welcomeCommand;

    @Autowired
    public DevCliCommand(WelcomeCommand welcomeCommand) {
        this.welcomeCommand = welcomeCommand;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public Integer call() throws Exception {
        // Default action when no subcommand is specified: run interactive welcome
        // command
        welcomeCommand.run();
        return 0;
    }
}
