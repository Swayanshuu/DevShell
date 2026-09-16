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
import java.util.stream.Collectors;

@Component
@Command(name = "export", description = "Export a comprehensive Developer DNA report to PDF, Markdown, JSON, or HTML", mixinStandardHelpOptions = true)
public class ExportCommand implements Runnable {

    @Option(names = {"-f", "--format"}, description = "Export format: pdf, markdown, json, or html (default: pdf)")
    private String format = "pdf";

    @Option(names = {"-o", "--output"}, description = "Target output file path")
    private String outputFile;

    private final LocalStorageService storageService;
    private final AnalysisEngine analysisEngine;
    private final InsightEngine insightEngine;
    private final AchievementEngine achievementEngine;
    private final DeveloperDnaEngine dnaEngine;
    private final JournalService journalService;
    private final AuthService authService;
    private final SyncService syncService;

    @Autowired
    public ExportCommand(LocalStorageService storageService,
                          AnalysisEngine analysisEngine,
                          InsightEngine insightEngine,
                          AchievementEngine achievementEngine,
                          DeveloperDnaEngine dnaEngine,
                          JournalService journalService,
                          AuthService authService,
                          SyncService syncService) {
        this.storageService = storageService;
        this.analysisEngine = analysisEngine;
        this.insightEngine = insightEngine;
        this.achievementEngine = achievementEngine;
        this.dnaEngine = dnaEngine;
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

        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (!downloadsDir.exists()) downloadsDir.mkdirs();
        File defaultFile = new File(downloadsDir, defaultFileName);
        String defaultPath = defaultFile.getAbsolutePath();

        String filePath = outputFile;
        if (filePath == null || filePath.trim().isEmpty()) {
            BoxRenderer.printAsciiBanner();
            System.out.println("  " + AnsiStyle.boldWhite("📄 Export Developer DNA Report"));
            System.out.print(AnsiStyle.boldCyan("  Enter target save path/directory [default: " + defaultPath + "]: "));
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
                filePath = defaultPath;
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
            System.out.println("  " + AnsiStyle.dim("Share your Developer DNA Card on LinkedIn, X, or GitHub! 🚀\n"));

        } catch (Exception e) {
            spinner.stopError("Failed to export report: " + e.getMessage());
            if (System.getProperty("devshell.debug") != null) {
                e.printStackTrace();
            }
        }
    }

    private void generatePdfReport(File targetFile, UserProfile user, List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages, int streak, List<Insight> insights, List<Learning> learnings, List<Bug> bugs) throws Exception {
        // 1-Page Compact Shareable Card Layout
        Document document = new Document(PageSize.A4, 28, 28, 28, 28);
        PdfWriter.getInstance(document, new FileOutputStream(targetFile));
        document.open();

        // Sleek Minimalist Dark Palette
        Color bgDarkHeader = new Color(13, 17, 23);       // #0D1117 (Jet Black)
        Color bgCard = new Color(22, 27, 34);             // #161B22 (Card Grey)
        Color borderDark = new Color(48, 54, 61);          // #30363D
        Color limeAccent = new Color(183, 255, 74);        // #B7FF4A (DevShell Green)
        Color cyanAccent = new Color(88, 166, 255);        // #58A6FF (Electric Blue)
        Color yellowAccent = new Color(210, 153, 34);      // #D29922 (Gold)
        Color textWhite = new Color(240, 246, 252);
        Color textMuted = new Color(139, 148, 158);

        // Typography
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, limeAccent);
        Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, cyanAccent);
        Font cardTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, textMuted);
        Font cardValueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, textWhite);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textWhite);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, textWhite);
        Font mutedFont = FontFactory.getFont(FontFactory.HELVETICA, 7, textMuted);

        // 1. TOP HEADER BRAND BANNER
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        headerTable.setSpacingAfter(8);

        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(bgDarkHeader);
        headerCell.setPadding(10);
        headerCell.setBorderColor(cyanAccent);
        headerCell.setBorderWidth(1.2f);

        Paragraph pTitle = new Paragraph("DEVSHELL DEVELOPER DNA CARD", titleFont);
        pTitle.setSpacingAfter(3);
        headerCell.addElement(pTitle);

        String username = user != null && user.getUsername() != null ? user.getUsername() : "Developer";
        String displayName = user != null && user.getName() != null ? user.getName() : username;
        String bioStr = user != null && user.getBio() != null ? user.getBio() : "Software Engineer";

        Paragraph pSub = new Paragraph("@" + username + " (" + displayName + ") • " + bioStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textWhite));
        pSub.setSpacingAfter(2);
        headerCell.addElement(pSub);

        Paragraph pMeta = new Paragraph("Verified Local Telemetry • Generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " • DevShell CLI (Swynx)", mutedFont);
        headerCell.addElement(pMeta);

        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Calculate DNA metrics
        DeveloperDnaEngine.DnaProfile dna = dnaEngine != null ? dnaEngine.analyzeDna(repos, commits, prs, analysisEngine) : null;
        String primaryDomain = dna != null ? dna.getPrimaryDomain() : "Software Development";

        // 2. HIGHLIGHT STAT CARDS GRID (3x2 Compact)
        PdfPTable statsGrid = new PdfPTable(3);
        statsGrid.setWidthPercentage(100);
        statsGrid.setSpacingAfter(10);
        statsGrid.setWidths(new float[]{1, 1, 1});

        int commitSum = repos.stream().mapToInt(Repository::getCommitCount).sum();
        int totalCommits = commits != null ? Math.max(commits.size(), commitSum) : commitSum;

        statsGrid.addCell(createCompactStatCard("ACTIVE STREAK", streak + " Days", "Consecutive Days", bgCard, yellowAccent, cardTitleFont, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, yellowAccent), mutedFont));
        statsGrid.addCell(createCompactStatCard("TOTAL COMMITS", totalCommits + " Commits", "Verified Commits", bgCard, limeAccent, cardTitleFont, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, limeAccent), mutedFont));
        statsGrid.addCell(createCompactStatCard("REPOSITORIES", repos.size() + " Repos", "Tracked Projects", bgCard, textWhite, cardTitleFont, cardValueFont, mutedFont));

        statsGrid.addCell(createCompactStatCard("PRIMARY DOMAIN", primaryDomain, "Specialization", bgCard, cyanAccent, cardTitleFont, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, cyanAccent), mutedFont));
        statsGrid.addCell(createCompactStatCard("KNOWLEDGE BASE", learnings.size() + " TIL Notes", "Learnings Saved", bgCard, textWhite, cardTitleFont, cardValueFont, mutedFont));
        statsGrid.addCell(createCompactStatCard("AUTH STATUS", "PAT Verified", "Local ~/.devshell/", bgCard, limeAccent, cardTitleFont, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, limeAccent), mutedFont));

        document.add(statsGrid);

        // 3. TECH STACK BREAKDOWN
        Paragraph pStackHeader = new Paragraph("TECH STACK SPECIALIZATION", sectionTitleFont);
        pStackHeader.setSpacingAfter(4);
        document.add(pStackHeader);

        PdfPTable langTable = new PdfPTable(3);
        langTable.setWidthPercentage(100);
        langTable.setSpacingAfter(10);
        langTable.setWidths(new float[]{2, 1, 3});

        if (dna != null && !dna.getCategoryBreakdown().isEmpty()) {
            for (Map.Entry<String, Double> entry : dna.getCategoryBreakdown().entrySet()) {
                double pct = entry.getValue();
                if (pct <= 0) continue;

                PdfPCell c1 = new PdfPCell(new Phrase(entry.getKey(), boldFont));
                c1.setBackgroundColor(bgCard); c1.setPadding(5); c1.setBorderColor(borderDark);

                PdfPCell c2 = new PdfPCell(new Phrase(String.format("%.1f%%", pct), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, limeAccent)));
                c2.setBackgroundColor(bgCard); c2.setPadding(5); c2.setBorderColor(borderDark);

                int bars = (int) Math.round((pct / 100.0) * 14);
                String barStr = "█".repeat(bars) + "░".repeat(Math.max(0, 14 - bars));
                PdfPCell c3 = new PdfPCell(new Phrase("[" + barStr + "]", FontFactory.getFont(FontFactory.COURIER, 7, cyanAccent)));
                c3.setBackgroundColor(bgCard); c3.setPadding(5); c3.setBorderColor(borderDark);

                langTable.addCell(c1); langTable.addCell(c2); langTable.addCell(c3);
            }
        }
        document.add(langTable);

        // 4. FEATURED PROJECTS (Top 3 active)
        Paragraph pReposHeader = new Paragraph("FEATURED REPOSITORIES", sectionTitleFont);
        pReposHeader.setSpacingAfter(4);
        document.add(pReposHeader);

        PdfPTable repoTable = new PdfPTable(3);
        repoTable.setWidthPercentage(100);
        repoTable.setSpacingAfter(10);
        repoTable.setWidths(new float[]{3, 2, 2});

        List<Repository> topRepos = repos.stream().limit(3).collect(Collectors.toList());
        for (Repository r : topRepos) {
            PdfPCell c1 = new PdfPCell(new Phrase(r.getName(), boldFont));
            PdfPCell c2 = new PdfPCell(new Phrase(r.getLanguage() != null ? r.getLanguage() : "General", normalFont));
            PdfPCell c3 = new PdfPCell(new Phrase(r.getStatus().getLabel(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, limeAccent)));
            c1.setBackgroundColor(bgCard); c1.setPadding(5); c1.setBorderColor(borderDark);
            c2.setBackgroundColor(bgCard); c2.setPadding(5); c2.setBorderColor(borderDark);
            c3.setBackgroundColor(bgCard); c3.setPadding(5); c3.setBorderColor(borderDark);
            repoTable.addCell(c1); repoTable.addCell(c2); repoTable.addCell(c3);
        }
        document.add(repoTable);

        // 5. FEATURED KNOWLEDGE BASE TIL DISCOVERY
        if (!learnings.isEmpty()) {
            Paragraph pLearnHeader = new Paragraph("FEATURED KNOWLEDGE BASE DISCOVERY", sectionTitleFont);
            pLearnHeader.setSpacingAfter(4);
            document.add(pLearnHeader);

            Learning l = learnings.get(0);
            PdfPTable learnTable = new PdfPTable(1);
            learnTable.setWidthPercentage(100);
            learnTable.setSpacingAfter(10);

            PdfPCell lCell = new PdfPCell();
            lCell.setBackgroundColor(bgCard);
            lCell.setPadding(6);
            lCell.setBorderColor(borderDark);

            Paragraph pTitleL = new Paragraph("[" + l.getId() + "] " + l.getTitle() + " (" + l.getProject() + ")", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, limeAccent));
            pTitleL.setSpacingAfter(2);
            lCell.addElement(pTitleL);

            Paragraph pDescL = new Paragraph(l.getContent(), normalFont);
            lCell.addElement(pDescL);

            learnTable.addCell(lCell);
            document.add(learnTable);
        }

        // 6. SOCIAL SHARE CALLOUT FOOTER
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(bgDarkHeader);
        footerCell.setPadding(6);
        footerCell.setBorderColor(limeAccent);
        footerCell.setBorderWidth(1f);

        Paragraph pFooter = new Paragraph("DevShell Personal Developer OS • Share on LinkedIn, X, GitHub • #DevShell #DeveloperDNA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, limeAccent));
        pFooter.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(pFooter);

        footerTable.addCell(footerCell);
        document.add(footerTable);

        document.close();
    }

    private PdfPCell createCompactStatCard(String label, String value, String subtext, Color bgColor, Color valColor, Font lblFont, Font valFont, Font subFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorderColor(new Color(48, 54, 61));

        Paragraph pLbl = new Paragraph(label, lblFont);
        pLbl.setSpacingAfter(1);
        cell.addElement(pLbl);

        Paragraph pVal = new Paragraph(value, valFont);
        pVal.setSpacingAfter(1);
        cell.addElement(pVal);

        Paragraph pSub = new Paragraph(subtext, subFont);
        cell.addElement(pSub);

        return cell;
    }

    private void writeStringToFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private String generateMarkdownReport(UserProfile user, List<Repository> repos, List<Commit> commits, List<PullRequest> prs, Map<String, Double> languages, int streak, List<Insight> insights, List<Learning> learnings, List<Bug> bugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("# DevShell Developer Report - @").append(user.getUsername()).append("\n\n");
        sb.append("> Generated on ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" by **DevShell (Swynx)**\n\n");

        sb.append("## Developer Profile\n");
        sb.append("- **Name**: ").append(user.getName()).append("\n");
        sb.append("- **Handle**: `@").append(user.getUsername()).append("`\n");
        if (user.getGithubId() > 0) sb.append("- **GitHub ID**: `#").append(user.getGithubId()).append("`\n");
        sb.append("- **Bio**: ").append(user.getBio()).append("\n");
        sb.append("- **Repositories**: ").append(repos.size()).append(" total\n");
        sb.append("- **Commit Streak**: ").append(streak).append(" days\n\n");

        sb.append("## Language Stack Distribution\n");
        languages.forEach((lang, pct) -> {
            sb.append(String.format("- **%-12s**: %5.1f%%\n", lang, pct));
        });
        sb.append("\n");

        sb.append("## Active Projects\n");
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
        sb.append("<h1>DevShell Developer Report - @").append(user.getUsername()).append("</h1>");
        sb.append("<div class='card'><h2>Profile</h2><p>Name: <strong>").append(user.getName()).append("</strong></p><p>Streak: <strong>").append(streak).append(" days</strong></p></div>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
