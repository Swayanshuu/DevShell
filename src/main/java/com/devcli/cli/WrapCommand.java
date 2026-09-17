package com.devcli.cli;

import com.devcli.wrap.WrapData;
import com.devcli.wrap.WrapRenderer;
import com.devcli.wrap.WrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;

@Component
@Command(name = "wrap", description = "Generate a shareable developer WRAP image.", mixinStandardHelpOptions = true)
public class WrapCommand implements Runnable {

    private final WrapService wrapService;
    private final WrapRenderer wrapRenderer;

    @Option(names = "--week", description = "Generate a WRAP for the last 7 days.")
    private boolean week;

    @Option(names = "--month", description = "Generate a WRAP for the last month.")
    private boolean month;

    @Option(names = "--year", description = "Generate a WRAP for the last year.")
    private boolean year;

    @Option(names = { "-o", "--output" }, description = "Custom output path for the generated WRAP image.")
    private String customOutput;

    @Autowired
    public WrapCommand(
            WrapService wrapService,
            WrapRenderer wrapRenderer) {
        this.wrapService = wrapService;
        this.wrapRenderer = wrapRenderer;
    }

    @Override
    public void run() {
        com.devcli.ui.LoadingSpinner spinner = null;
        try {
            String period = determinePeriod();

            System.out.println();
            spinner = com.devcli.ui.LoadingSpinner.start(" " + com.devcli.ui.AnsiStyle.boldCyan("[  0%]") + " Initializing " + period.toUpperCase() + " WRAP...");

            com.devcli.ui.LoadingSpinner activeSpinner = spinner;
            WrapData data = wrapService.generateWrap(period, (pct, status) -> {
                String colorPct = String.format("[%3d%%]", pct);
                activeSpinner.updateMessage(" " + com.devcli.ui.AnsiStyle.boldCyan(colorPct) + " " + status);
            });

            Path output = resolveOutputPath(period);

            spinner.updateMessage(" " + com.devcli.ui.AnsiStyle.boldCyan("[ 95%]") + " Rendering 1080x1920 WRAP canvas...");
            wrapRenderer.render(data, output);

            spinner.stopSuccess("WRAP generated successfully (100%)!");

            System.out.println();
            System.out.println("  Period: " + period.toUpperCase());
            System.out.println("  Output: " + output.toAbsolutePath());
            System.out.println();

        } catch (Exception e) {
            if (spinner != null) {
                spinner.stopError("Failed to generate WRAP.");
            } else {
                System.out.println();
                System.out.println("✗ Failed to generate WRAP.");
            }
            System.out.println("  " + e.getMessage());
        }
    }

    private Path resolveOutputPath(String period) {
        if (customOutput != null && !customOutput.isBlank()) {
            return Path.of(customOutput);
        }
        String filename;
        switch (period.toLowerCase()) {
            case "month" -> filename = "devshell-m-wrap.png";
            case "year" -> filename = "devshell-y-wrap.png";
            default -> filename = "devshell-w-wrap.png";
        }
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            Path downloadsDir = Path.of(userHome, "Downloads");
            if (java.nio.file.Files.exists(downloadsDir) || downloadsDir.toFile().mkdirs()) {
                return downloadsDir.resolve(filename);
            }
        }
        return Path.of(filename);
    }

    private String determinePeriod() {

        int selected = 0;

        if (week)
            selected++;
        if (month)
            selected++;
        if (year)
            selected++;

        if (selected > 1) {
            throw new IllegalArgumentException(
                    "Choose only one period: --week, --month, or --year.");
        }

        if (month)
            return "month";
        if (year)
            return "year";

        // Default
        return "week";
    }
}