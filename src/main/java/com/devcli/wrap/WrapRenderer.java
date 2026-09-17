package com.devcli.wrap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

@Service
public class WrapRenderer {

    private static final Color COLOR_NEON_GREEN = new Color(118, 238, 0); // Reference Lime Green #76EE00
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_LABEL = new Color(190, 195, 200);
    private static final Color COLOR_TAGLINE = new Color(200, 210, 200);
    private static final Color COLOR_CARD_BG = new Color(15, 25, 20, 210);

    public void render(WrapData data, Path outputPath) throws Exception {

        InputStream input = getResourceStream("/wrap/themes/devshell/devshell-bg.png");

        if (input == null) {
            throw new IllegalStateException("DevShell WRAP background not found.");
        }

        BufferedImage backgroundAsset = ImageIO.read(input);
        BufferedImage background = new BufferedImage(1080, 1920, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = background.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(backgroundAsset, 0, 0, 1080, 1920, null);

        InputStream layoutInput = getResourceStream("/wrap/themes/devshell/layout.json");

        if (layoutInput == null) {
            throw new IllegalStateException("WRAP layout not found.");
        }

        JsonNode layout = new ObjectMapper().readTree(layoutInput);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. HEADER (SOFTWARE ENGINEER + GIANT WRAP + PERIOD + TAGLINE)
        JsonNode header = layout.get("header");
        if (header != null) {
            int centerX = header.get("centerX").asInt(540);
            int y = header.get("y").asInt(150);

            // Title: SOFTWARE ENGINEER (Bold, clean white uppercase matching reference poster)
            g.setFont(new Font("SansSerif", Font.BOLD, 38));
            g.setColor(COLOR_WHITE);
            FontMetrics fmTitle = g.getFontMetrics();
            g.drawString("SOFTWARE ENGINEER", centerX - (fmTitle.stringWidth("SOFTWARE ENGINEER") / 2), y);

            // Giant Title: WRAP
            g.setFont(new Font("SansSerif", Font.BOLD, 125));
            g.setColor(COLOR_NEON_GREEN);
            FontMetrics fmWrap = g.getFontMetrics();
            int wrapWidth = fmWrap.stringWidth("WRAP");
            int wrapX = centerX - (wrapWidth / 2) - 25;
            int wrapY = y + 120;
            g.drawString("WRAP", wrapX, wrapY);

            // Script Period: e.g. 2026 or WEEK / MONTH / YEAR
            g.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 46));
            g.setColor(new Color(212, 225, 87));
            g.drawString(data.period(), wrapX + wrapWidth + 12, wrapY - 35);

            // Tagline: Code, Commits, and Repos
            g.setFont(new Font("Serif", Font.ITALIC, 26));
            g.setColor(COLOR_TAGLINE);
            String tagline = header.path("tagline").asText("Code, Commits, and Repos");
            FontMetrics fmTag = g.getFontMetrics();
            g.drawString(tagline, centerX - (fmTag.stringWidth(tagline) / 2), wrapY + 45);
        }

        // 2. TWO-COLUMN STATS GRID WITH ICONS AND LIME NEON NUMBERS
        JsonNode stats = layout.get("stats");
        if (stats != null) {
            drawStatItem(g, stats.get("linesChanged"), formatNumber(data.linesChanged()), "</>");
            drawStatItem(g, stats.get("commits"), formatNumber(data.commits()), "☍");
            drawStatItem(g, stats.get("additions"), "+" + formatNumber(data.additions()), "☕");
            drawStatItem(g, stats.get("deletions"), "-" + formatNumber(data.deletions()), "⏱");
            drawStatItem(g, stats.get("activeDays"), formatNumber(data.activeDays()), "📅");
            drawStatItem(g, stats.get("longestStreak"), formatNumber(data.longestStreak()), "🔥");
            drawStatItem(g, stats.get("projects"), formatNumber(data.projects()), "🚀");
            drawStatItem(g, stats.get("codingHours"), formatHours(data.codingHours()), "⚡");
        }

        // 3. PROFILE (POLAROID PHOTO FRAME AT BOTTOM LEFT)
        JsonNode profile = layout.get("profile");
        if (profile != null) {
            drawPolaroidProfile(g, profile.get("polaroid"), data.avatarUrl(), data.displayName(), data.username());
        }

        // 4. HIGHLIGHT CARDS (MOST USED LANGUAGE & MOST ACTIVE PROJECT AT BOTTOM RIGHT)
        JsonNode highlights = layout.get("highlights");
        if (highlights != null) {
            drawHighlightCard(g, highlights.get("topLanguage"), data.topLanguage());
            drawHighlightCard(g, highlights.get("topRepository"), data.topRepository());
        }

        g.dispose();

        ImageIO.write(background, "png", outputPath.toFile());
        System.out.println("WRAP image generated: " + outputPath.toAbsolutePath());
    }

    private void drawStatItem(Graphics2D g, JsonNode config, String valueStr, String iconSymbol) {
        if (config == null)
            return;
        int x = config.get("x").asInt();
        int y = config.get("y").asInt();
        int maxWidth = config.get("maxWidth").asInt();
        int maxFontSize = config.get("maxFontSize").asInt();
        int minFontSize = config.get("minFontSize").asInt();
        String label = config.get("label").asText();

        // Icon + Label Line
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(COLOR_NEON_GREEN);
        g.drawString(iconSymbol, x, y);

        int iconOffset = g.getFontMetrics().stringWidth(iconSymbol + " ");
        g.setColor(COLOR_LABEL);
        g.drawString(label, x + iconOffset, y);

        // Huge Lime-Green Value
        drawResponsiveText(
                g,
                valueStr,
                x,
                y + 68,
                maxWidth,
                maxFontSize,
                minFontSize,
                Font.BOLD,
                COLOR_NEON_GREEN);
    }

    private void drawHighlightCard(Graphics2D g, JsonNode config, String valueText) {
        if (config == null)
            return;
        int x = config.get("x").asInt();
        int y = config.get("y").asInt();
        int maxWidth = config.get("maxWidth").asInt();
        String label = config.get("label").asText();

        String[] items = (valueText != null && !valueText.isBlank()) ? valueText.split(",") : new String[0];
        int cardHeight = 210;

        // Card Backdrop
        g.setColor(COLOR_CARD_BG);
        g.fill(new RoundRectangle2D.Float(x - 20, y - 40, maxWidth, cardHeight, 20, 20));

        // Left Neon Accent Strip
        g.setColor(COLOR_NEON_GREEN);
        g.fill(new RoundRectangle2D.Float(x - 20, y - 40, 8, cardHeight, 4, 4));

        // Label
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(COLOR_LABEL);
        g.drawString(label, x, y - 10);

        // Render Stacked List Items (#1, #2, #3)
        int startY = y + 36;
        int rowHeight = 48;

        if (items.length == 0) {
            drawResponsiveText(g, "N/A", x, startY, maxWidth - 30, 24, 16, Font.BOLD, COLOR_WHITE);
            return;
        }

        for (int i = 0; i < Math.min(items.length, 3); i++) {
            String item = items[i].trim();
            if (item.isEmpty()) continue;

            int currentY = startY + (i * rowHeight);

            // Rank Badge: #1, #2, #3
            String rank = "#" + (i + 1);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));

            if (i == 0) {
                g.setColor(COLOR_NEON_GREEN);
            } else if (i == 1) {
                g.setColor(new Color(180, 235, 120));
            } else {
                g.setColor(COLOR_LABEL);
            }
            g.drawString(rank, x, currentY);

            int rankWidth = g.getFontMetrics().stringWidth("#1  ");

            // Responsive item text on its own line
            drawResponsiveText(
                    g,
                    item,
                    x + rankWidth,
                    currentY,
                    maxWidth - rankWidth - 30,
                    24,
                    16,
                    Font.BOLD,
                    COLOR_WHITE);
        }
    }

    private void drawPolaroidProfile(Graphics2D g, JsonNode pConfig, String avatarUrl, String displayName,
            String username) {
        if (pConfig == null)
            return;
        int x = pConfig.get("x").asInt();
        int y = pConfig.get("y").asInt();
        int w = pConfig.get("width").asInt();
        int h = pConfig.get("height").asInt();
        double rotationDeg = pConfig.path("rotationDeg").asDouble(-3.0);

        AffineTransform oldTx = g.getTransform();
        g.rotate(Math.toRadians(rotationDeg), x + (w / 2.0), y + (h / 2.0));

        // Polaroid Frame Drop Shadow
        g.setColor(new Color(0, 0, 0, 140));
        g.fill(new RoundRectangle2D.Float(x + 6, y + 8, w, h, 12, 12));

        // Polaroid White Background Frame
        g.setColor(new Color(248, 248, 246));
        g.fill(new RoundRectangle2D.Float(x, y, w, h, 12, 12));

        // Inner Photo Cutout Bounds
        int photoMargin = 20;
        int photoW = w - (photoMargin * 2);
        int photoH = h - 110;
        int photoX = x + photoMargin;
        int photoY = y + photoMargin;

        g.setColor(new Color(25, 25, 25));
        g.fillRect(photoX, photoY, photoW, photoH);

        // Load Avatar into Photo Cutout
        BufferedImage avatarImg = loadAvatarImage(avatarUrl, username);

        if (avatarImg != null) {
            g.drawImage(avatarImg, photoX, photoY, photoW, photoH, null);
        } else {
            // Initial placeholder in photo box
            g.setColor(new Color(35, 45, 40));
            g.fillRect(photoX, photoY, photoW, photoH);
            g.setColor(COLOR_NEON_GREEN);
            String initial = (displayName != null && !displayName.isBlank())
                    ? displayName.substring(0, 1).toUpperCase()
                    : "D";
            g.setFont(new Font("SansSerif", Font.BOLD, 72));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initial, photoX + (photoW - fm.stringWidth(initial)) / 2,
                    photoY + (photoH + fm.getAscent()) / 2 - 10);
        }

        // Top Red Push Pin Detail
        g.setColor(new Color(220, 40, 40));
        g.fillOval(x + (w / 2) - 8, y + 6, 16, 16);
        g.setColor(new Color(255, 120, 120));
        g.fillOval(x + (w / 2) - 4, y + 8, 6, 6);

        // Polaroid Caption (Display Name & Handle)
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(new Color(20, 20, 20));
        g.drawString(displayName != null ? displayName : "Developer", photoX, y + h - 50);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g.setColor(new Color(90, 90, 90));
        g.drawString(username != null ? username : "@devshell", photoX, y + h - 22);

        g.setTransform(oldTx);
    }

    public void drawResponsiveText(
            Graphics2D g,
            String text,
            int x,
            int y,
            int maxWidth,
            int maxFontSize,
            int minFontSize,
            int fontStyle,
            Color color) {
        if (text == null)
            text = "";
        int fontSize = maxFontSize;
        Font font = new Font("SansSerif", fontStyle, fontSize);
        FontMetrics fm = g.getFontMetrics(font);

        while (fm.stringWidth(text) > maxWidth && fontSize > minFontSize) {
            fontSize--;
            font = new Font("SansSerif", fontStyle, fontSize);
            fm = g.getFontMetrics(font);
        }

        String drawStr = text;
        if (fm.stringWidth(drawStr) > maxWidth) {
            while (drawStr.length() > 3 && fm.stringWidth(drawStr + "...") > maxWidth) {
                drawStr = drawStr.substring(0, drawStr.length() - 1);
            }
            if (drawStr.length() < text.length()) {
                drawStr += "...";
            }
        }

        g.setFont(font);
        g.setColor(color);
        g.drawString(drawStr, x, y);
    }

    public static String formatNumber(long number) {
        if (number < 10_000) {
            return String.format("%,d", number);
        } else if (number < 1_000_000) {
            double k = number / 1000.0;
            return (k % 1 == 0) ? String.format("%.0fK", k) : String.format("%.1fK", k);
        } else {
            double m = number / 1000000.0;
            return (m % 1 == 0) ? String.format("%.0fM", m) : String.format("%.1fM", m);
        }
    }

    public static String formatHours(double hours) {
        if (hours == 0) return "0h";
        if (hours % 1 == 0) {
            return String.format("%.0fh", hours);
        } else {
            return String.format("%.1fh", hours);
        }
    }

    private BufferedImage loadAvatarImage(String avatarUrl, String username) {
        java.util.List<String> candidateUrls = new java.util.ArrayList<>();
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            candidateUrls.add(avatarUrl);
        }
        if (username != null && !username.isBlank()) {
            String cleanUser = username.startsWith("@") ? username.substring(1) : username;
            candidateUrls.add("https://github.com/" + cleanUser + ".png");
            candidateUrls.add("https://avatars.githubusercontent.com/" + cleanUser);
        }

        if (candidateUrls.isEmpty()) {
            return null;
        }

        // 1. Try Java HttpClient with fast timeout
        for (String urlStr : candidateUrls) {
            try {
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                        .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                        .connectTimeout(java.time.Duration.ofSeconds(2))
                        .build();

                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(URI.create(urlStr))
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Accept", "image/png,image/jpeg,image/*,*/*")
                        .timeout(java.time.Duration.ofSeconds(2))
                        .GET()
                        .build();

                java.net.http.HttpResponse<byte[]> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
                if (resp.statusCode() == 200 && resp.body() != null && resp.body().length > 0) {
                    try (InputStream is = new java.io.ByteArrayInputStream(resp.body())) {
                        BufferedImage img = ImageIO.read(is);
                        if (img != null) {
                            return img;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Fallback: System curl / curl.exe
        for (String urlStr : candidateUrls) {
            try {
                ProcessBuilder pb = new ProcessBuilder("curl", "-s", "-L", "-m", "5", urlStr);
                Process process = pb.start();
                try (InputStream is = process.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    if (bytes != null && bytes.length > 500) {
                        try (java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes)) {
                            BufferedImage img = ImageIO.read(bais);
                            if (img != null) {
                                return img;
                            }
                        }
                    }
                }
                process.waitFor();
            } catch (Exception ignored) {}
        }

        return null;
    }

    private InputStream getResourceStream(String resourcePath) {
        String pathNoLeadingSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        String pathWithLeadingSlash = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;

        try {
            InputStream is = getClass().getResourceAsStream(pathWithLeadingSlash);
            if (is != null) return is;
        } catch (Exception ignored) {}

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                InputStream is = cl.getResourceAsStream(pathNoLeadingSlash);
                if (is != null) return is;
            }
        } catch (Exception ignored) {}

        try {
            ClassLoader cl = WrapRenderer.class.getClassLoader();
            if (cl != null) {
                InputStream is = cl.getResourceAsStream(pathNoLeadingSlash);
                if (is != null) return is;
            }
        } catch (Exception ignored) {}

        try {
            org.springframework.core.io.ClassPathResource res = new org.springframework.core.io.ClassPathResource(pathNoLeadingSlash);
            if (res.exists()) {
                return res.getInputStream();
            }
        } catch (Exception ignored) {}

        return null;
    }
}
