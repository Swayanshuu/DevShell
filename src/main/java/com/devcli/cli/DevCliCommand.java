package com.devcli.cli;

import com.devcli.ui.AnsiStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Component
@Command(
        name = "devshell",
        aliases = {"devcli"},
        description = "DevShell - Personal Developer Command Center",
        subcommands = {
                StatusCommand.class,
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
                ExportCommand.class
        },
        mixinStandardHelpOptions = true,
        versionProvider = DevCliCommand.VersionProvider.class
)
public class DevCliCommand implements Callable<Integer> {

    public static class VersionProvider implements CommandLine.IVersionProvider {
        @Override
        public String[] getVersion() {
            return new String[]{ "DevShell " + com.devcli.service.UpdateCheckerService.getCurrentVersion() + " (Swynx)" };
        }
    }

    @Option(names = {"--debug"}, description = "Enable detailed technical debug output and trace logs", scope = CommandLine.ScopeType.INHERIT)
    private boolean debug;

    private final StatusCommand statusCommand;

    @Autowired
    public DevCliCommand(StatusCommand statusCommand) {
        this.statusCommand = statusCommand;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public Integer call() throws Exception {
        // Default action when no subcommand is specified: run status command
        statusCommand.run();
        return 0;
    }
}
