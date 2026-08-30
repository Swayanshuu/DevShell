package com.devcli.cli;

import com.devcli.model.*;
import com.devcli.service.*;
import com.devcli.storage.LocalStorageService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import com.devcli.ui.LoadingSpinner;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
@Command(name = "export", aliases = {"report"}, description = "Export a comprehensive Developer DNA report to PDF, Markdown, JSON, or HTML", mixinStandardHelpOptions = true)
public class ExportCommand implements Runnable {

    @Option(names = {"-f", "--format"}, description = "Export format: pdf, markdown, json, or html (default: pdf)")
    private String format = "pdf";

    @Option(names = {"-o", "--output"}, description = "Target output file path")
    private String outputFile;

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final InsightEngine insightEngine;
    private final AchievementEngine achievementEngine;
    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public ExportCommand(LocalStorageService storageService, AnalysisEngine analysisEngine, InsightEngine insightEngine, AchievementEngine achievementEngine, JournalService journalService, AuthService authService, SyncService syncService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.insightEngine = insightEngine;
        this.achievementEngine = achievementEngine;
        this.journalService = journalService;
        this.authService = authService;
        this.syncService = syncService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        UserProfile user = storageService.getUserProfile();
        List<Repository> repos = storageService.getRepositories();
        List<Commit> commits = storageService.getCommits();
        List<PullRequest> prs = storageService.getPullRequests();
        List<Learning> learnings = journalService.getLearnings();
        List<Bug> bugs = journalService.getBugs();
        Map<String, Double> languages = analysisEngine.calculateLanguagePercentages(repos);
        int streak = analysisEngine.calculateStreak(commits);
        List<Insight> insights = insightEngine.generateInsights(repos, commits, prs, languages);

        String fmt = format != null ? format.toLowerCase() : "pdf";
        String ext = "pdf".equals(fmt) ? "pdf" : "json".equals(fmt) ? "json" : "html".equals(fmt) ? "html" : "md";
        String defaultFileName = "devshell-report." + ext;

        String filePath = outputFile;
        if (filePath == null || filePath.trim().isEmpty()) {
            BoxRenderer.printAsciiBanner();
            System.out.println("  " + AnsiStyle.boldWhite("📄 Export Developer DNA Report"));
            System.out.print(AnsiStyle.boldCyan("  Enter target save path/directory [default: ./" + defaultFileName + "]: "));
            Scanner scanner = new Scanner(System.in);
            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
            if (!input.isEmpty()) {
                File inputPath = new File(input);
                if (inputPath.isDirectory()) {
                    filePath = new File(inputPath, defaultFileName).getAbsolutePath();
                } else {
                    filePath = input;
                }
            } else {
                filePath = defaultFileName;
            }
        }

        LoadingSpinner spinner = LoadingSpinner.start("Generating Developer PDF Report...");

        try {
            File targetFile = new File(filePath);
            if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }

            if ("pdf".equals(fmt)) {
                generatePdfReport(targetFile, user, repos, commits, prs, languages, streak, insights, learnings, bugs);
            } else if ("json".equals(fmt)) {
                writeStringToFile(targetFile, generateJsonReport(user, repos, commits, prs, languages, streak, insights, learnings, bugs));
            } else if ("html".equals(fmt)) {
                writeStringToFile(targetFile, generateHtmlReport(user, repos, commits, prs, languages, streak, insights, learnings, bugs));
            } else {
                writeStringToFile(targetFile, generateMarkdownReport(user, repos, commits, prs, languages, streak, insights, learnings, bugs));
            }

            spinner.stopSuccess("Developer report generated successfully!");
            System.out.println("\n  " + AnsiStyle.boldGreen("✓ Report exported to:"));
            System.out.println("  " + AnsiStyle.boldCyan(targetFile.getAbsolutePath()));
            System.out.println("  " + AnsiStyle.dim("Share your report with recruiters, teams, or showcase on GitHub! 🚀\n"));

        } catch (Exception e) {
            spinner.stopError("Failed to export report: " + e.getMessage());
            if (System.getProperty("devshell.debug") != null) {
                e.printStackTrace();
            }
        }
    }

    private void generatePdfReport(File targetFile, UserProfile user, List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages, int streak, List<Insight> insights, List<Learning> learnings, List<Bug> bugs) throws Exception {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(targetFile));
        document.open();

        // Colors
        Color primaryColor = new Color(9, 105, 218); // Blue
        Color darkBg = new Color(13, 17, 23);
        Color lightGray = new Color(246, 248, 250);
        Color textColor = new Color(36, 41, 47);

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, primaryColor);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, primaryColor);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, textColor);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, textColor);

        // Header Title
        Paragraph header = new Paragraph("DevShell Developer DNA Report", titleFont);
        header.setAlignment(Element.ALIGN_LEFT);
        document.add(header);

        Paragraph subHeader = new Paragraph("Generated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " • DevShell by Swynx", subTitleFont);
        subHeader.setSpacingAfter(15);
        document.add(subHeader);

        // User Profile Card Table
        PdfPTable profileTable = new PdfPTable(2);
        profileTable.setWidthPercentage(100);
        profileTable.setSpacingAfter(15);

        PdfPCell cellLeft = new PdfPCell();
        cellLeft.setBackgroundColor(lightGray);
        cellLeft.setPadding(10);
        cellLeft.setBorder(Rectangle.NO_BORDER);

        cellLeft.addElement(new Paragraph("Developer: @" + user.getUsername(), boldFont));
        cellLeft.addElement(new Paragraph("Name: " + (user.getName() != null ? user.getName() : user.getUsername()), normalFont));
        if (user.getGithubId() > 0) {
            cellLeft.addElement(new Paragraph("GitHub ID: #" + user.getGithubId(), normalFont));
        }
        cellLeft.addElement(new Paragraph("Bio: " + (user.getBio() != null ? user.getBio() : "Developer"), normalFont));

        PdfPCell cellRight = new PdfPCell();
        cellRight.setBackgroundColor(lightGray);
        cellRight.setPadding(10);
        cellRight.setBorder(Rectangle.NO_BORDER);
        cellRight.addElement(new Paragraph("Commit Streak: " + streak + " days", boldFont));
        cellRight.addElement(new Paragraph("Total Repositories: " + repos.size(), normalFont));
        cellRight.addElement(new Paragraph("Tracked Commits: " + commits.size(), normalFont));

        profileTable.addCell(cellLeft);
        profileTable.addCell(cellRight);
        document.add(profileTable);

        // Language Distribution
        Paragraph langHeader = new Paragraph("Tech Stack Distribution", sectionHeaderFont);
        langHeader.setSpacingBefore(10);
        langHeader.setSpacingAfter(8);
        document.add(langHeader);

        PdfPTable langTable = new PdfPTable(2);
        langTable.setWidthPercentage(100);
        langTable.setSpacingAfter(15);
        langTable.setWidths(new float[]{1, 3});

        languages.forEach((lang, pct) -> {
            PdfPCell c1 = new PdfPCell(new Phrase(lang, boldFont));
            c1.setPadding(6);
            c1.setBackgroundColor(Color.WHITE);

            PdfPCell c2 = new PdfPCell(new Phrase(String.format("%.1f%%", pct), normalFont));
            c2.setPadding(6);
            c2.setBackgroundColor(Color.WHITE);

            langTable.addCell(c1);
            langTable.addCell(c2);
        });
        document.add(langTable);

        // Active Projects
        Paragraph projHeader = new Paragraph("Active Repositories", sectionHeaderFont);
        projHeader.setSpacingBefore(10);
        projHeader.setSpacingAfter(8);
        document.add(projHeader);

        PdfPTable projTable = new PdfPTable(3);
        projTable.setWidthPercentage(100);
        projTable.setSpacingAfter(15);
        projTable.setWidths(new float[]{2, 1, 1});

        PdfPCell h1 = new PdfPCell(new Phrase("Repository Name", boldFont));
        PdfPCell h2 = new PdfPCell(new Phrase("Language", boldFont));
        PdfPCell h3 = new PdfPCell(new Phrase("Status", boldFont));
        h1.setBackgroundColor(lightGray); h1.setPadding(6);
        h2.setBackgroundColor(lightGray); h2.setPadding(6);
        h3.setBackgroundColor(lightGray); h3.setPadding(6);
        projTable.addCell(h1); projTable.addCell(h2); projTable.addCell(h3);

        for (Repository r : repos) {
            PdfPCell p1 = new PdfPCell(new Phrase(r.getName(), normalFont));
            PdfPCell p2 = new PdfPCell(new Phrase(r.getLanguage() != null ? r.getLanguage() : "N/A", normalFont));
            PdfPCell p3 = new PdfPCell(new Phrase(r.getStatus().getLabel(), normalFont));
            p1.setPadding(5); p2.setPadding(5); p3.setPadding(5);
            projTable.addCell(p1); projTable.addCell(p2); projTable.addCell(p3);
        }
        document.add(projTable);

        // Developer Insights & Journal
        if (!learnings.isEmpty()) {
            Paragraph learnHeader = new Paragraph("Developer Journal & Learnings", sectionHeaderFont);
            learnHeader.setSpacingBefore(10);
            learnHeader.setSpacingAfter(8);
            document.add(learnHeader);

            for (Learning l : learnings) {
                Paragraph item = new Paragraph("• " + l.getTitle() + ": " + l.getDescription(), normalFont);
                item.setSpacingAfter(4);
                document.add(item);
            }
        }

        document.close();
    }

    private void writeStringToFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private String generateMarkdownReport(UserProfile user, List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages, int streak, List<Insight> insights, List<Learning> learnings, List<Bug> bugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 👨‍💻 DevShell Developer Report - @").append(user.getUsername()).append("\n\n");
        sb.append("> Generated on ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" by **DevShell (Swynx)**\n\n");

        sb.append("## 👤 Developer Profile\n");
        sb.append("- **Name**: ").append(user.getName()).append("\n");
        sb.append("- **Handle**: `@").append(user.getUsername()).append("`\n");
        if (user.getGithubId() > 0) sb.append("- **GitHub ID**: `#").append(user.getGithubId()).append("`\n");
        sb.append("- **Bio**: ").append(user.getBio()).append("\n");
        sb.append("- **Repositories**: ").append(repos.size()).append(" total\n");
        sb.append("- **Commit Streak**: ").append(streak).append(" days 🔥\n\n");

        sb.append("## 💻 Language Stack Distribution\n");
        languages.forEach((lang, pct) -> {
            sb.append(String.format("- **%-12s**: %5.1f%%\n", lang, pct));
        });
        sb.append("\n");

        sb.append("## 📊 Active Projects\n");
        for (Repository r : repos) {
            sb.append("- **").append(r.getName()).append("** (").append(r.getLanguage()).append(") - ").append(r.getStatus().getLabel()).append("\n");
            if (r.getDescription() != null && !r.getDescription().isEmpty()) {
                sb.append("  *").append(r.getDescription()).append("*\n");
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String generateJsonReport(UserProfile user, List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages, int streak, List<Insight> insights, List<Learning> learnings, List<Bug> bugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"developer\": {\n");
        sb.append("    \"username\": \"").append(user.getUsername()).append("\",\n");
        sb.append("    \"name\": \"").append(user.getName()).append("\",\n");
        sb.append("    \"githubId\": ").append(user.getGithubId()).append(",\n");
        sb.append("    \"commitStreak\": ").append(streak).append("\n");
        sb.append("  },\n");
        sb.append("  \"totalRepositories\": ").append(repos.size()).append(",\n");
        sb.append("  \"languages\": {\n");
        int count = 0;
        for (Map.Entry<String, Double> entry : languages.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            count++;
            if (count < languages.size()) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String generateHtmlReport(UserProfile user, List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages, int streak, List<Insight> insights, List<Learning> learnings, List<Bug> bugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><title>DevShell Report - @").append(user.getUsername()).append("</title>");
        sb.append("<style>body{font-family:sans-serif;margin:40px;background:#0d1117;color:#c9d1d9;}h1,h2{color:#58a6ff;}.card{background:#161b22;padding:20px;border-radius:8px;margin-bottom:20px;border:1px solid #30363d;}</style></head><body>");
        sb.append("<h1>👨‍💻 DevShell Developer Report - @").append(user.getUsername()).append("</h1>");
        sb.append("<div class='card'><h2>Profile</h2><p>Name: <strong>").append(user.getName()).append("</strong></p><p>Streak: <strong>").append(streak).append(" days 🔥</strong></p></div>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
