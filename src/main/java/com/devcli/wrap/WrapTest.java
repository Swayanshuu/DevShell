package com.devcli.wrap;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.nio.file.Path;

@ComponentScan("com.devcli")
public class WrapTest {

    public static void main(String[] args) throws Exception {

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(WrapTest.class)) {

            WrapService wrapService = context.getBean(WrapService.class);
            WrapRenderer wrapRenderer = context.getBean(WrapRenderer.class);

            WrapData data;
            try {
                data = wrapService.generateWrap("year");
            } catch (IllegalStateException e) {
                System.out.println("User not logged in, generating WRAP preview using test data...");
                data = new WrapData(
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
            }

            Path outputPath = Path.of("devshell-wrap-test.png");
            wrapRenderer.render(data, outputPath);
            System.out.println("✓ WrapTest completed successfully: " + outputPath.toAbsolutePath());
        }
    }
}