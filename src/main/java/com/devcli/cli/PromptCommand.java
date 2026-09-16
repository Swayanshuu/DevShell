package com.devcli.cli;

import com.devcli.model.Commit;
import com.devcli.service.AnalysisEngine;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.util.List;

@Component
@Command(name = "prompt", aliases = {"shell"}, description = "Show contextual developer prompt snippet for zsh/bash/fish shell integration", mixinStandardHelpOptions = true)
public class PromptCommand implements Runnable {

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;

    @Autowired
    public PromptCommand(LocalStorageService storageService, AnalysisEngine analysisEngine) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
    }

    @Override
    public void run() {
        List<Commit> commits = storageService.getCommits();
        List<Commit> todayCommits = analysisEngine.getTodayCommits(commits);

        System.out.println();
        List<String> promptLines = new java.util.ArrayList<>();
        promptLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Current Shell Status")), AnsiStyle.cyan("~/projects/devshell main ✓ | " + todayCommits.size() + " commits today")));
        promptLines.add(String.format("  %s %s", AnsiStyle.dim(String.format("%-22s", "Prompt Symbol")), AnsiStyle.boldWhite("$")));
        promptLines.add("");
        promptLines.add("  " + AnsiStyle.dim("To add DevShell prompt integration to your ~/.zshrc or ~/.bashrc:"));
        promptLines.add("  " + AnsiStyle.boldYellow("export PROMPT='$(devshell prompt --short) '$PROMPT"));

        BoxRenderer.renderBox("SHELL PROMPT INTEGRATION", promptLines, AnsiStyle.CYAN);
        System.out.println();
    }
}
