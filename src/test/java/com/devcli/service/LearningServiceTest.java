package com.devcli.service;

import com.devcli.model.Learning;
import com.devcli.storage.LocalStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LearningServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;
    private LearningService learningService;

    @BeforeEach
    public void setUp() throws Exception {
        storageService = new LocalStorageService();
        // Use reflection to override baseDir in LocalStorageService to tempDir for isolated testing
        Field baseDirField = LocalStorageService.class.getDeclaredField("baseDir");
        baseDirField.setAccessible(true);
        baseDirField.set(storageService, tempDir);

        learningService = new LearningService(storageService);
    }

    @Test
    public void testCreateLearningAndIdGeneration() {
        Learning l1 = learningService.addLearning("Spring Security", "Security filter chain details", List.of("spring", "security"), "LinkPeer", "Backend");
        assertNotNull(l1);
        assertEquals("LEARN-001", l1.getId());
        assertEquals("Spring Security", l1.getTitle());
        assertEquals("LinkPeer", l1.getProject());
        assertEquals(2, l1.getTags().size());

        Learning l2 = learningService.addLearning(null, "Docker container networking rules", List.of("docker", "devops"), "DevShell", "DevOps");
        assertNotNull(l2);
        assertEquals("LEARN-002", l2.getId());
        assertEquals("Docker container networking rules", l2.getTitle());
    }

    @Test
    public void testFindByIdFlexibility() {
        learningService.addLearning("PostgreSQL Pooling", "HikariCP configuration", List.of("db"), "DevShell", "Database");

        Optional<Learning> opt1 = learningService.findById("LEARN-001");
        assertTrue(opt1.isPresent());

        Optional<Learning> opt2 = learningService.findById("learn-001");
        assertTrue(opt2.isPresent());

        Optional<Learning> opt3 = learningService.findById("1");
        assertTrue(opt3.isPresent());

        Optional<Learning> opt4 = learningService.findById("learn-1");
        assertTrue(opt4.isPresent());

        Optional<Learning> opt5 = learningService.findById("LEARN-999");
        assertFalse(opt5.isPresent());
    }

    @Test
    public void testUpdatePreservesCreatedAt() throws Exception {
        Learning created = learningService.addLearning("Initial Title", "Initial Content", List.of("tag1"), "Proj1", "Cat1");
        LocalDateTime originalCreatedAt = created.getCreatedAt();

        // Pause briefly to ensure timestamp difference
        Thread.sleep(10);

        Learning updated = learningService.updateLearning("LEARN-001", "New Title", "New Content", List.of("tag1", "tag2"), "Proj2", "Cat2");
        assertNotNull(updated);
        assertEquals("LEARN-001", updated.getId());
        assertEquals("New Title", updated.getTitle());
        assertEquals("New Content", updated.getContent());
        assertEquals("Proj2", updated.getProject());
        assertEquals(2, updated.getTags().size());
        assertEquals(originalCreatedAt, updated.getCreatedAt(), "createdAt should remain unchanged after edit");
        assertTrue(updated.getUpdatedAt().isAfter(originalCreatedAt) || updated.getUpdatedAt().equals(originalCreatedAt));
    }

    @Test
    public void testPinAndUnpin() {
        learningService.addLearning("Title 1", "Content 1", null, "General", "General");
        
        boolean pinned = learningService.setPinned("LEARN-001", true);
        assertTrue(pinned);

        Optional<Learning> opt = learningService.findById("LEARN-001");
        assertTrue(opt.isPresent());
        assertTrue(opt.get().isPinned());

        learningService.setPinned("LEARN-001", false);
        assertFalse(learningService.findById("LEARN-001").get().isPinned());
    }

    @Test
    public void testSearchByQueryTagAndProject() {
        learningService.addLearning("Spring Security Filters", "Authentication filter execution order", List.of("spring", "security"), "LinkPeer", "Backend");
        learningService.addLearning("PostgreSQL Indexing", "B-Tree index optimization", List.of("postgres", "database"), "LinkPeer", "Database");
        learningService.addLearning("Docker Multistage", "Optimizing container sizes", List.of("docker", "devops"), "DevShell", "DevOps");

        List<Learning> springResults = learningService.searchLearnings("spring", null, null);
        assertEquals(1, springResults.size());
        assertEquals("LEARN-001", springResults.get(0).getId());

        List<Learning> tagResults = learningService.searchLearnings(null, "database", null);
        assertEquals(1, tagResults.size());
        assertEquals("LEARN-002", tagResults.get(0).getId());

        List<Learning> projectResults = learningService.searchLearnings(null, null, "LinkPeer");
        assertEquals(2, projectResults.size());
    }

    @Test
    public void testDeleteLearning() {
        learningService.addLearning("To Delete", "Content", null, "General", "General");
        assertTrue(learningService.findById("LEARN-001").isPresent());

        boolean deleted = learningService.deleteLearning("LEARN-001");
        assertTrue(deleted);
        assertFalse(learningService.findById("LEARN-001").isPresent());
    }

    @Test
    public void testLegacyDataMigration() {
        // Create legacy records in storage service without IDs or tags
        List<Learning> legacyList = new ArrayList<>();
        Learning legacy = new Learning();
        legacy.setTitle("Legacy Note");
        legacy.setDescription("Legacy content description");
        legacy.setCreatedAt(LocalDateTime.now().minusDays(5));
        legacyList.add(legacy);

        storageService.saveLearnings(legacyList);

        // Service load should trigger migration
        List<Learning> migrated = learningService.getAllLearnings();
        assertEquals(1, migrated.size());
        Learning m = migrated.get(0);
        assertEquals("LEARN-001", m.getId());
        assertEquals("Legacy Note", m.getTitle());
        assertEquals("Legacy content description", m.getContent());
        assertNotNull(m.getTags());
        assertEquals("General", m.getProject());
    }
}
