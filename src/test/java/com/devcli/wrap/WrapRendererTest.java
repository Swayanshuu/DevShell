package com.devcli.wrap;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WrapRendererTest {

    @Test
    public void testRenderPngPoster() throws Exception {
        WrapRenderer renderer = new WrapRenderer();
        WrapData data = new WrapData(
                "2026",
                1842,
                48.5,
                12400,
                3200,
                15600,
                142,
                18,
                8,
                "Java, JavaScript, Dart",
                "LeetCode-Solutions, DevShell-Landing, devcli",
                "@swayanshuu",
                "Swayam",
                "https://github.com/github.png"
        );

        Path outputPath = Path.of("devshell-wrap.png");
        renderer.render(data, outputPath);

        assertTrue(Files.exists(outputPath), "devshell-wrap.png should be generated");
        assertTrue(Files.size(outputPath) > 0, "devshell-wrap.png should not be empty");

        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(outputPath.toFile());
        assertEquals(1080, img.getWidth(), "Image width must be 1080");
        assertEquals(1920, img.getHeight(), "Image height must be 1920");
    }
}
