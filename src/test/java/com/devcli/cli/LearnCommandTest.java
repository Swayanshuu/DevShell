package com.devcli.cli;

import com.devcli.model.Learning;
import com.devcli.service.AuthService;
import com.devcli.service.GitHubService;
import com.devcli.service.LearningService;
import com.devcli.service.SyncService;
import com.devcli.storage.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LearnCommandTest {

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;
    private LearningService learningService;
    private GitHubService gitHubService;
    private AuthService authService;
    private SyncService syncService;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() throws Exception {
        storageService = new LocalStorageService();
        Field baseDirField = LocalStorageService.class.getDeclaredField("baseDir");
        baseDirField.setAccessible(true);
        baseDirField.set(storageService, tempDir);

        learningService = new LearningService(storageService);
        gitHubService = new GitHubService();
        authService = new AuthService(storageService, gitHubService);
        syncService = new SyncService(storageService, gitHubService);

        com.devcli.model.UserProfile user = new com.devcli.model.UserProfile();
        user.setUsername("testuser");
        storageService.saveUserProfile(user);
    }

    @Test
    public void testDirectPositionalSyntaxAdd() {
        LearnCommand cmd = new LearnCommand(learningService, authService, syncService);
        CommandLine cl = new CommandLine(cmd);
        
        cl.execute("Spring", "Security", "filter", "chain", "order");

        List<Learning> list = learningService.getAllLearnings();
        assertEquals(1, list.size());
        assertEquals("LEARN-001", list.get(0).getId());
        assertTrue(list.get(0).getContent().contains("Spring Security filter chain order"));
    }

    @Test
    public void testSubcommandAddWithFlags() {
        LearnCommand cmd = new LearnCommand(learningService, authService, syncService);
        CommandLine cl = new CommandLine(cmd);

        cl.execute("add", "--title", "JWT Authentication", "--tags", "spring,security", "--project", "LinkPeer", "Implemented JWT authentication filter");

        List<Learning> list = learningService.getAllLearnings();
        assertEquals(1, list.size());
        Learning l = list.get(0);
        assertEquals("LEARN-001", l.getId());
        assertEquals("JWT Authentication", l.getTitle());
        assertEquals("LinkPeer", l.getProject());
        assertTrue(l.getTags().contains("spring"));
        assertTrue(l.getTags().contains("security"));
    }

    @Test
    public void testJsonOutputFlag() {
        learningService.addLearning("JSON Test", "Content for JSON", List.of("json"), "DevShell", "Backend");

        System.setOut(new PrintStream(outContent));
        try {
            LearnCommand cmd = new LearnCommand(learningService, authService, syncService);
            CommandLine cl = new CommandLine(cmd);
            cl.execute("--json");

            String output = outContent.toString();
            assertTrue(output.contains("\"id\" : \"LEARN-001\"") || output.contains("\"id\": \"LEARN-001\""));
            assertTrue(output.contains("\"title\" : \"JSON Test\"") || output.contains("\"title\": \"JSON Test\""));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPinAndUnpinSubcommands() {
        learningService.addLearning("Test Pin", "Content", null, "General", "General");

        LearnCommand cmd = new LearnCommand(learningService, authService, syncService);
        CommandLine cl = new CommandLine(cmd);
        cl.execute("pin", "LEARN-001");

        assertTrue(learningService.findById("LEARN-001").get().isPinned());

        cl.execute("unpin", "LEARN-001");
        assertFalse(learningService.findById("LEARN-001").get().isPinned());
    }

    @Test
    public void testDeleteSubcommandWithYesFlag() {
        learningService.addLearning("Test Delete", "Content", null, "General", "General");

        LearnCommand cmd = new LearnCommand(learningService, authService, syncService);
        CommandLine cl = new CommandLine(cmd);
        cl.execute("delete", "LEARN-001", "--yes");

        assertFalse(learningService.findById("LEARN-001").isPresent());
    }
}
