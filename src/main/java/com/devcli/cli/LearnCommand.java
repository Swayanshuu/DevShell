package com.devcli.cli;

import com.devcli.model.Learning;
import com.devcli.service.AuthService;
import com.devcli.service.LearningService;
import com.devcli.service.SyncService;
import com.devcli.ui.AnsiStyle;
import com.devcli.ui.BoxRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Command(
    name = "learn",
    aliases = {"journal", "kb"},
    description = "Terminal-first Developer Knowledge Base & Journal",
    mixinStandardHelpOptions = true,
    subcommands = {
        LearnCommand.AddSubcommand.class,
        LearnCommand.ViewSubcommand.class,
        LearnCommand.EditSubcommand.class,
        LearnCommand.DeleteSubcommand.class,
        LearnCommand.SearchSubcommand.class,
        LearnCommand.PinSubcommand.class,
        LearnCommand.UnpinSubcommand.class
    }
)
public class LearnCommand implements Runnable {

    @Option(names = {"--search"}, description = "Search stored developer learnings by query")
    private String searchQuery;

    @Option(names = {"-t", "--tag"}, description = "Filter learnings by tag")
    private String tagFilter;

    @Option(names = {"-p", "--project"}, description = "Filter learnings by project")
    private String projectFilter;

    @Option(names = {"-c", "--category"}, description = "Specify category (e.g. Backend, Database, CLI)")
    private String categoryFilter;

    @Option(names = {"--json"}, description = "Output valid machine-readable JSON")
    private boolean jsonOutput;

    @Parameters(arity = "0..*", description = "Subcommand or learning content to record")
    private String[] inputParts;

    private final LearningService learningService;
    private final AuthService authService;
    private final SyncService syncService;
    private final ObjectMapper jsonMapper;

    @Autowired
    public LearnCommand(LearningService learningService, AuthService authService, SyncService syncService) {
        this.learningService = learningService;
        this.authService = authService;
        this.syncService = syncService;
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.registerModule(new JavaTimeModule());
        this.jsonMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public LearningService getLearningService() {
        return learningService;
    }

    @Override
    public void run() {
        if (!authService.ensureAuthenticated(syncService)) {
            return;
        }

        // If input parts provided without explicit subcommand, treat as "add <content>"
        if (inputParts != null && inputParts.length > 0) {
            String firstWord = inputParts[0].toLowerCase().trim();
            if (!isSubcommandName(firstWord)) {
                String content = String.join(" ", inputParts).trim();
                Learning created = learningService.addLearning(null, content, null, projectFilter, categoryFilter);
                if (jsonOutput) {
                    printJson(created);
                } else {
                    System.out.println(AnsiStyle.boldGreen("\n✓ Recorded learning ") + AnsiStyle.boldCyan("[" + created.getId() + "]") + AnsiStyle.boldWhite(": " + created.getTitle()));
                    System.out.println(AnsiStyle.dim("Developer Knowledge Base updated.\n"));
                }
                return;
            }
        }

        // Search query or list view
        List<Learning> learnings;
        if (searchQuery != null || tagFilter != null || projectFilter != null || categoryFilter != null) {
            learnings = learningService.searchLearnings(searchQuery, tagFilter, projectFilter);
        } else {
            learnings = learningService.getAllLearnings();
        }

        if (jsonOutput) {
            printJson(learnings);
            return;
        }

        displayKnowledgeBaseList(learnings);
    }

    private boolean isSubcommandName(String word) {
        return word.equals("add") || word.equals("view") || word.equals("edit") ||
               word.equals("delete") || word.equals("rm") || word.equals("search") ||
               word.equals("pin") || word.equals("unpin");
    }

    private void displayKnowledgeBaseList(List<Learning> learnings) {
        System.out.println();
        List<String> lines = new ArrayList<>();

        if (learnings.isEmpty()) {
            lines.add("  " + AnsiStyle.gray("No learnings recorded yet. Run `devshell learn \"Your note\"` to create one."));
        } else {
            List<Learning> pinned = learnings.stream().filter(Learning::isPinned).collect(java.util.stream.Collectors.toList());
            List<Learning> recent = learnings.stream().filter(l -> !l.isPinned()).collect(java.util.stream.Collectors.toList());

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

            if (!pinned.isEmpty()) {
                lines.add("  " + AnsiStyle.boldYellow("PINNED"));
                lines.add("");
                for (Learning l : pinned) {
                    String dateStr = l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "";
                    String projStr = l.getProject() != null && !"General".equalsIgnoreCase(l.getProject()) ? "[" + l.getProject() + "]" : "";
                    lines.add(String.format("  ★ %s  %s %s",
                            AnsiStyle.boldYellow(String.format("%-10s", l.getId())),
                            AnsiStyle.boldWhite(String.format("%-28s", l.getTitle())),
                            AnsiStyle.dim(dateStr)));
                    if (!projStr.isEmpty() || !l.getTags().isEmpty()) {
                        String tagsStr = l.getTags().isEmpty() ? "" : "Tags: " + String.join(", ", l.getTags());
                        lines.add(String.format("             %s %s", AnsiStyle.boldCyan(projStr), AnsiStyle.gray(tagsStr)));
                    }
                    lines.add("");
                }
            }

            if (!recent.isEmpty()) {
                if (!pinned.isEmpty()) {
                    lines.add("  " + AnsiStyle.dim("──────────────────────────────────────────────────"));
                }
                lines.add("  " + AnsiStyle.boldCyan("RECENT"));
                lines.add("");
                for (Learning l : recent) {
                    String dateStr = l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "";
                    String projStr = l.getProject() != null && !"General".equalsIgnoreCase(l.getProject()) ? "[" + l.getProject() + "]" : "";
                    lines.add(String.format("    %s  %s %s",
                            AnsiStyle.boldCyan(String.format("%-10s", l.getId())),
                            AnsiStyle.boldWhite(String.format("%-28s", l.getTitle())),
                            AnsiStyle.dim(dateStr)));
                    if (!projStr.isEmpty() || !l.getTags().isEmpty()) {
                        String tagsStr = l.getTags().isEmpty() ? "" : "Tags: " + String.join(", ", l.getTags());
                        lines.add(String.format("             %s %s", AnsiStyle.boldCyan(projStr), AnsiStyle.gray(tagsStr)));
                    }
                }
            }
        }

        LearningService.KnowledgeBaseStats stats = learningService.getStats();
        lines.add("");
        lines.add("  " + AnsiStyle.dim("──────────────────────────────────────────────────"));
        lines.add("  " + AnsiStyle.dim(stats.totalLearnings + " learnings | " + stats.uniqueProjects + " projects | " + stats.totalTags + " tags | " + stats.pinnedCount + " pinned"));

        BoxRenderer.renderBox("DEVELOPER KNOWLEDGE BASE", lines, AnsiStyle.CYAN);
        System.out.println();
    }

    private void printJson(Object obj) {
        try {
            System.out.println(jsonMapper.writeValueAsString(obj));
        } catch (Exception e) {
            System.out.println("{}");
        }
    }

    // ==========================================
    // SUBCOMMAND BASE CLASS
    // ==========================================

    @CommandLine.Command
    public static abstract class AbstractLearnSubcommand implements Runnable {
        @ParentCommand
        protected LearnCommand parent;

        @Autowired
        protected LearningService learningService;

        public AbstractLearnSubcommand() {}

        public AbstractLearnSubcommand(LearningService learningService) {
            this.learningService = learningService;
        }

        protected LearningService getService() {
            if (this.learningService != null) return this.learningService;
            if (this.parent != null && this.parent.getLearningService() != null) return this.parent.getLearningService();
            throw new IllegalStateException("LearningService unavailable");
        }
    }

    // ==========================================
    // SUBCOMMANDS
    // ==========================================

    @Component
    @Command(name = "add", description = "Add a new technical learning or note")
    public static class AddSubcommand extends AbstractLearnSubcommand {
        @Parameters(arity = "1..*", description = "Content of the learning")
        private String[] contentParts;

        @Option(names = {"--title"}, description = "Explicit title for the learning")
        private String title;

        @Option(names = {"--tags"}, description = "Comma-separated tags (e.g., spring,security)")
        private String tagsStr;

        @Option(names = {"--project"}, description = "Associated project name")
        private String project;

        @Option(names = {"--category"}, description = "Category name")
        private String category;

        @Option(names = {"--json"}, description = "Output valid machine-readable JSON")
        private boolean jsonOutput;

        public AddSubcommand() {}
        public AddSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            String content = contentParts != null ? String.join(" ", contentParts).trim() : "";
            List<String> tags = tagsStr != null ? Arrays.asList(tagsStr.split(",")) : new ArrayList<>();

            Learning created = getService().addLearning(title, content, tags, project, category);
            if (jsonOutput) {
                try {
                    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).enable(SerializationFeature.INDENT_OUTPUT);
                    System.out.println(mapper.writeValueAsString(created));
                } catch (Exception ignored) {}
            } else {
                System.out.println(AnsiStyle.boldGreen("\n✓ Recorded learning ") + AnsiStyle.boldCyan("[" + created.getId() + "]") + AnsiStyle.boldWhite(": " + created.getTitle()));
                System.out.println(AnsiStyle.dim("Developer Knowledge Base updated.\n"));
            }
        }
    }

    @Component
    @Command(name = "view", description = "View full details of a specific learning")
    public static class ViewSubcommand extends AbstractLearnSubcommand {
        @Parameters(index = "0", arity = "1", description = "Learning ID (e.g. LEARN-042 or 42)")
        private String id;

        @Option(names = {"--json"}, description = "Output valid machine-readable JSON")
        private boolean jsonOutput;

        public ViewSubcommand() {}
        public ViewSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            Optional<Learning> opt = getService().findById(id);
            if (opt.isEmpty()) {
                System.out.println(AnsiStyle.brightRed("\n✗ Learning '" + id + "' not found in Knowledge Base.\n"));
                return;
            }

            Learning l = opt.get();
            if (jsonOutput) {
                try {
                    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).enable(SerializationFeature.INDENT_OUTPUT);
                    System.out.println(mapper.writeValueAsString(l));
                } catch (Exception ignored) {}
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            List<String> lines = new ArrayList<>();
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("ID:"), AnsiStyle.boldYellow(l.getId())));
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("TITLE:"), AnsiStyle.boldWhite(l.getTitle())));
            lines.add("");
            lines.add("  " + AnsiStyle.boldCyan("CONTENT:"));
            for (String cLine : l.getContent().split("\r?\n")) {
                lines.add("    " + AnsiStyle.brightWhite(cLine));
            }
            lines.add("");
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("TAGS:"), AnsiStyle.boldCyan(l.getTags().isEmpty() ? "None" : String.join(", ", l.getTags()))));
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("PROJECT:"), AnsiStyle.boldGreen(l.getProject())));
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("CATEGORY:"), AnsiStyle.boldMagenta(l.getCategory())));
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("CREATED:"), AnsiStyle.gray(l.getCreatedAt() != null ? l.getCreatedAt().format(fmt) : "N/A")));
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("UPDATED:"), AnsiStyle.gray(l.getUpdatedAt() != null ? l.getUpdatedAt().format(fmt) : "N/A")));
            lines.add(String.format("  %-12s %s", AnsiStyle.dim("STATUS:"), l.isPinned() ? AnsiStyle.boldYellow("★ PINNED") : AnsiStyle.dim("NORMAL")));

            BoxRenderer.renderBox("LEARNING DETAILS: " + l.getId(), lines, AnsiStyle.CYAN);
            System.out.println();
        }
    }

    @Component
    @Command(name = "edit", description = "Edit an existing learning entry")
    public static class EditSubcommand extends AbstractLearnSubcommand {
        @Parameters(index = "0", arity = "1", description = "Learning ID (e.g. LEARN-042 or 42)")
        private String id;

        @Option(names = {"--title"}, description = "New title for learning")
        private String newTitle;

        @Option(names = {"--content"}, description = "New content for learning")
        private String newContent;

        @Option(names = {"--tags"}, description = "New comma-separated tags")
        private String newTagsStr;

        @Option(names = {"--project"}, description = "New project name")
        private String newProject;

        @Option(names = {"--category"}, description = "New category name")
        private String newCategory;

        @Option(names = {"--json"}, description = "Output valid machine-readable JSON")
        private boolean jsonOutput;

        public EditSubcommand() {}
        public EditSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            Optional<Learning> opt = getService().findById(id);
            if (opt.isEmpty()) {
                System.out.println(AnsiStyle.brightRed("\n✗ Learning '" + id + "' not found in Knowledge Base.\n"));
                return;
            }

            Learning existing = opt.get();

            // Interactive prompt if no explicit flags provided
            if (newTitle == null && newContent == null && newTagsStr == null && newProject == null && newCategory == null) {
                Scanner scanner = new Scanner(System.in);
                System.out.println();
                System.out.println("  " + AnsiStyle.boldCyan("EDIT LEARNING: " + existing.getId()));
                System.out.println("  " + AnsiStyle.dim("────────────────────────────────────────"));
                System.out.println();

                System.out.println("  Current Title: " + AnsiStyle.boldWhite(existing.getTitle()));
                System.out.print("  New Title (press Enter to keep): ");
                String inputTitle = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                if (!inputTitle.isEmpty()) newTitle = inputTitle;

                System.out.println("\n  Current Content: " + AnsiStyle.brightWhite(existing.getContent()));
                System.out.print("  New Content (press Enter to keep): ");
                String inputContent = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                if (!inputContent.isEmpty()) newContent = inputContent;

                System.out.println("\n  Current Tags: " + AnsiStyle.boldCyan(String.join(",", existing.getTags())));
                System.out.print("  New Tags (press Enter to keep): ");
                String inputTags = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                if (!inputTags.isEmpty()) newTagsStr = inputTags;

                System.out.println("\n  Current Project: " + AnsiStyle.boldGreen(existing.getProject()));
                System.out.print("  New Project (press Enter to keep): ");
                String inputProj = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                if (!inputProj.isEmpty()) newProject = inputProj;
            }

            List<String> newTags = newTagsStr != null ? Arrays.asList(newTagsStr.split(",")) : null;
            Learning updated = getService().updateLearning(existing.getId(), newTitle, newContent, newTags, newProject, newCategory);

            if (jsonOutput) {
                try {
                    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).enable(SerializationFeature.INDENT_OUTPUT);
                    System.out.println(mapper.writeValueAsString(updated));
                } catch (Exception ignored) {}
            } else {
                System.out.println(AnsiStyle.boldGreen("\n✓ Learning ") + AnsiStyle.boldCyan("[" + updated.getId() + "]") + AnsiStyle.boldGreen(" updated successfully.\n"));
            }
        }
    }

    @Component
    @Command(name = "delete", aliases = {"rm"}, description = "Delete a learning entry")
    public static class DeleteSubcommand extends AbstractLearnSubcommand {
        @Parameters(index = "0", arity = "1", description = "Learning ID to delete (e.g. LEARN-042 or 42)")
        private String id;

        @Option(names = {"-y", "--yes"}, description = "Skip deletion confirmation prompt")
        private boolean confirmYes;

        public DeleteSubcommand() {}
        public DeleteSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            Optional<Learning> opt = getService().findById(id);
            if (opt.isEmpty()) {
                System.out.println(AnsiStyle.brightRed("\n✗ Learning '" + id + "' not found in Knowledge Base.\n"));
                return;
            }

            Learning target = opt.get();

            if (!confirmYes) {
                System.out.println();
                System.out.println("  " + AnsiStyle.boldYellow("Delete learning " + target.getId() + " (" + target.getTitle() + ")?"));
                System.out.print("  " + AnsiStyle.boldCyan("[y] Yes  [n] No > "));
                Scanner scanner = new Scanner(System.in);
                String choice = scanner.hasNextLine() ? scanner.nextLine().trim().toLowerCase() : "";
                if (!choice.equalsIgnoreCase("y") && !choice.equalsIgnoreCase("yes")) {
                    System.out.println(AnsiStyle.dim("\n  Deletion cancelled.\n"));
                    return;
                }
            }

            boolean deleted = getService().deleteLearning(target.getId());
            if (deleted) {
                System.out.println(AnsiStyle.boldGreen("\n✓ Learning ") + AnsiStyle.boldCyan("[" + target.getId() + "]") + AnsiStyle.boldGreen(" deleted.\n"));
            } else {
                System.out.println(AnsiStyle.brightRed("\n✗ Failed to delete learning '" + id + "'.\n"));
            }
        }
    }

    @Component
    @Command(name = "search", description = "Search knowledge base entries by title, content, tag, or project")
    public static class SearchSubcommand extends AbstractLearnSubcommand {
        @Parameters(arity = "0..1", description = "Search query string")
        private String query;

        @Option(names = {"-t", "--tag"}, description = "Filter by tag")
        private String tag;

        @Option(names = {"-p", "--project"}, description = "Filter by project")
        private String project;

        @Option(names = {"--json"}, description = "Output valid machine-readable JSON")
        private boolean jsonOutput;

        public SearchSubcommand() {}
        public SearchSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            List<Learning> results = getService().searchLearnings(query, tag, project);

            if (jsonOutput) {
                try {
                    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule()).enable(SerializationFeature.INDENT_OUTPUT);
                    System.out.println(mapper.writeValueAsString(results));
                } catch (Exception ignored) {}
                return;
            }

            System.out.println();
            List<String> lines = new ArrayList<>();
            if (results.isEmpty()) {
                lines.add("  " + AnsiStyle.gray("No learnings found matching search criteria."));
            } else {
                for (Learning l : results) {
                    lines.add(String.format("  %s  %s %s",
                            AnsiStyle.boldCyan(String.format("%-10s", l.getId())),
                            AnsiStyle.boldWhite(String.format("%-28s", l.getTitle())),
                            AnsiStyle.boldGreen("[" + l.getProject() + "]")));
                    if (!l.getTags().isEmpty()) {
                        lines.add("             " + AnsiStyle.gray("Tags: " + String.join(", ", l.getTags())));
                    }
                }
            }

            String searchHeader = query != null ? "SEARCH RESULTS: " + query : tag != null ? "TAG RESULTS: " + tag : project != null ? "PROJECT RESULTS: " + project : "SEARCH RESULTS";
            BoxRenderer.renderBox(searchHeader + " (" + results.size() + ")", lines, AnsiStyle.CYAN);
            System.out.println();
        }
    }

    @Component
    @Command(name = "pin", description = "Pin an important learning to the top of knowledge base")
    public static class PinSubcommand extends AbstractLearnSubcommand {
        @Parameters(index = "0", arity = "1", description = "Learning ID to pin (e.g. LEARN-042 or 42)")
        private String id;

        public PinSubcommand() {}
        public PinSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            boolean success = getService().setPinned(id, true);
            if (success) {
                System.out.println(AnsiStyle.boldGreen("\n✓ Learning ") + AnsiStyle.boldCyan("[" + id + "]") + AnsiStyle.boldGreen(" pinned to top.\n"));
            } else {
                System.out.println(AnsiStyle.brightRed("\n✗ Learning '" + id + "' not found.\n"));
            }
        }
    }

    @Component
    @Command(name = "unpin", description = "Unpin a learning entry")
    public static class UnpinSubcommand extends AbstractLearnSubcommand {
        @Parameters(index = "0", arity = "1", description = "Learning ID to unpin (e.g. LEARN-042 or 42)")
        private String id;

        public UnpinSubcommand() {}
        public UnpinSubcommand(LearningService service) { super(service); }

        @Override
        public void run() {
            boolean success = getService().setPinned(id, false);
            if (success) {
                System.out.println(AnsiStyle.boldGreen("\n✓ Learning ") + AnsiStyle.boldCyan("[" + id + "]") + AnsiStyle.boldGreen(" unpinned.\n"));
            } else {
                System.out.println(AnsiStyle.brightRed("\n✗ Learning '" + id + "' not found.\n"));
            }
        }
    }
}
