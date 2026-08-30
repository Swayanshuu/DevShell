package com.devcli.service;

import com.devcli.ui.AnsiStyle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class UpdateCheckerService {

    private static String cachedVersion;
    private final Path cacheFile;
    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    public UpdateCheckerService() {
        Path baseDir = Paths.get(System.getProperty("user.home"), ".devshell");
        this.cacheFile = baseDir.resolve("update.json");
        this.mapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public static synchronized String getCurrentVersion() {
        if (cachedVersion == null) {
            cachedVersion = loadVersionFromResources();
        }
        return cachedVersion;
    }

    private static String loadVersionFromResources() {
        try (InputStream is = UpdateCheckerService.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                Properties prop = new Properties();
                prop.load(is);
                String ver = prop.getProperty("info.app.version");
                if (ver != null && !ver.isEmpty() && !ver.contains("@")) {
                    return ver.trim();
                }
            }
        } catch (Exception ignored) {}

        try (InputStream is = UpdateCheckerService.class.getResourceAsStream("/META-INF/maven/com.swynx/devshell/pom.properties")) {
            if (is != null) {
                Properties prop = new Properties();
                prop.load(is);
                String ver = prop.getProperty("version");
                if (ver != null && !ver.isEmpty()) {
                    return ver.trim();
                }
            }
        } catch (Exception ignored) {}

        Package pkg = UpdateCheckerService.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion().trim();
        }

        return "1.0.0";
    }

    public void checkAndNotify() {
        try {
            String appVersion = getCurrentVersion();
            Map<String, Object> cache = readCache();
            String latestVersion = cache.get("latestVersion") != null ? cache.get("latestVersion").toString() : appVersion;
            long lastChecked = cache.get("lastChecked") instanceof Number ? ((Number) cache.get("lastChecked")).longValue() : 0L;

            if (isNewerVersion(appVersion, latestVersion)) {
                System.out.println();
                System.out.println("  " + AnsiStyle.boldYellow("💡 Update available: ") + AnsiStyle.boldWhite(appVersion) + AnsiStyle.dim(" → ") + AnsiStyle.boldGreen(latestVersion));
                System.out.println("  " + AnsiStyle.cyan("👉 Run `npm install -g devshell` to update to the latest version.\n"));
            }

            // Check async if cache is older than 4 hours
            long FOUR_HOURS_MS = 4 * 60 * 60 * 1000L;
            if (System.currentTimeMillis() - lastChecked > FOUR_HOURS_MS) {
                Thread t = new Thread(this::fetchLatestFromNpm);
                t.setDaemon(true);
                t.start();
            }
        } catch (Exception ignored) {}
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readCache() {
        try {
            File file = cacheFile.toFile();
            if (file.exists()) {
                return mapper.readValue(file, Map.class);
            }
        } catch (Exception ignored) {}
        return new HashMap<>();
    }

    private void fetchLatestFromNpm() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://registry.npmjs.org/devshell/latest"))
                    .header("Accept", "application/json")
                    .header("User-Agent", "DevCLI-UpdateChecker")
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                String npmVersion = root.path("version").asText(null);
                if (npmVersion != null && !npmVersion.isEmpty()) {
                    Map<String, Object> newCache = new HashMap<>();
                    newCache.put("latestVersion", npmVersion);
                    newCache.put("lastChecked", System.currentTimeMillis());

                    File file = cacheFile.toFile();
                    if (file.getParentFile() != null && !file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    mapper.writeValue(file, newCache);
                }
            }
        } catch (Exception ignored) {}
    }

    public static boolean isNewerVersion(String current, String latest) {
        if (current == null || latest == null) return false;
        String[] cParts = current.split("\\.");
        String[] lParts = latest.split("\\.");
        int length = Math.max(cParts.length, lParts.length);

        for (int i = 0; i < length; i++) {
            int c = i < cParts.length ? parseOrZero(cParts[i]) : 0;
            int l = i < lParts.length ? parseOrZero(lParts[i]) : 0;
            if (l > c) return true;
            if (l < c) return false;
        }
        return false;
    }

    private static int parseOrZero(String val) {
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
