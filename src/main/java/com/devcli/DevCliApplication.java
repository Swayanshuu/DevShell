package com.devcli;

import com.devcli.cli.DevCliCommand;
import com.devcli.ui.AnsiStyle;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevCliApplication implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final DevCliCommand devCliCommand;
    private final com.devcli.service.UpdateCheckerService updateCheckerService;
    private final com.devcli.service.SyncService syncService;
    private int exitCode;

    @Autowired
    public DevCliApplication(IFactory factory,
                             DevCliCommand devCliCommand,
                             com.devcli.service.UpdateCheckerService updateCheckerService,
                             com.devcli.service.SyncService syncService) {
        this.factory = factory;
        this.devCliCommand = devCliCommand;
        this.updateCheckerService = updateCheckerService;
        this.syncService = syncService;
    }

    public static void main(String[] args) {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "chcp 65001 > nul").start().waitFor();
            } catch (Exception ignored) {}
        }
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.stdout.encoding", "UTF-8");
        System.setProperty("sun.stderr.encoding", "UTF-8");
        System.setProperty("spring.main.banner-mode", "off");

        java.util.List<String> springArgs = new java.util.ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--debug".equalsIgnoreCase(arg)) {
                System.setProperty("devshell.debug", "true");
            } else if ("--no-color".equalsIgnoreCase(arg)) {
                com.devcli.ui.AnsiStyle.setColorEnabled(false);
                springArgs.add(arg);
            } else if ("--theme".equalsIgnoreCase(arg) && i + 1 < args.length) {
                String next = args[i + 1].toLowerCase();
                if ("white".equals(next) || "light".equals(next)) {
                    com.devcli.ui.AnsiStyle.setColorEnabled(false);
                }
                springArgs.add(arg);
            } else {
                springArgs.add(arg);
            }
        }

        try {
            System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
            System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
        System.exit(SpringApplication.exit(SpringApplication.run(DevCliApplication.class, springArgs.toArray(new String[0]))));
    }

    @Override
    public void run(String... args) {
        if (args.length > 0 && ("-h".equals(args[0]) || "--help".equals(args[0]) || "help".equals(args[0]))) {
            com.devcli.cli.HelpCommand.printHelpScreen();
            exitCode = 0;
            updateCheckerService.checkAndNotify();
            return;
        }

        boolean shouldAutoSync = true;
        if (args.length > 0) {
            String firstArg = args[0].toLowerCase();
            if ("login".equals(firstArg) || "logout".equals(firstArg) || "help".equals(firstArg) || "sync".equals(firstArg)
                    || "-h".equals(firstArg) || "--help".equals(firstArg) || "-v".equals(firstArg) || "--version".equals(firstArg)) {
                shouldAutoSync = false;
            }
        }

        com.devcli.ui.LoadingSpinner spinner = null;
        java.util.concurrent.CompletableFuture<Void> syncFuture = null;
        if (shouldAutoSync) {
            spinner = com.devcli.ui.LoadingSpinner.start("Syncing developer workspace...");
            syncFuture = syncService.triggerAutoSync();
        }

        CommandLine cmdLine = new CommandLine(devCliCommand, factory);

        // Custom exception handler to provide clean error UX unless --debug is specified
        cmdLine.setExecutionExceptionHandler((ex, cmd, parseResult) -> {
            boolean debug = parseResult.matchedOption("debug") != null || System.getProperty("devshell.debug") != null || devCliCommand.isDebug();
            System.out.println(AnsiStyle.brightRed("\n✗ Could not complete operation."));
            System.out.println(AnsiStyle.boldYellow("\nPossible causes:"));
            System.out.println("  • Network connection unavailable or timed out");
            System.out.println("  • GitHub authorization expired or invalid");
            System.out.println("  • Command syntax error");
            System.out.println(AnsiStyle.cyan("\nTry running:"));
            System.out.println("  devshell login\n");

            if (debug) {
                System.out.println(AnsiStyle.boldRed("--- [DEBUG STACK TRACE] ---"));
                ex.printStackTrace();
            }
            return 1;
        });

        if (syncFuture != null) {
            try {
                syncFuture.get(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }

        if (spinner != null) {
            spinner.stopClear();
        }

        exitCode = cmdLine.execute(args);

        updateCheckerService.checkAndNotify();
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
