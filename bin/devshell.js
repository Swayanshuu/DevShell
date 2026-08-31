#!/usr/bin/env node

const { spawnSync, execSync } = require("child_process");
const path = require("path");
const fs = require("fs");

// Ensure Windows Console uses UTF-8 Code Page 65001
if (process.platform === "win32") {
  try {
    execSync("chcp 65001", { stdio: "ignore" });
  } catch (e) {}
}

const jarPath = path.join(__dirname, "..", "target", "devshell-1.0.13.jar");
const projectDir = path.join(__dirname, "..");

// Check if Java runtime is available
try {
  execSync("java -version", { stdio: "ignore" });
} catch (e) {
  console.error(
    "\x1b[31m%s\x1b[0m",
    "✗ Error: Java 17+ JRE/JDK is required to run DevShell.",
  );
  console.error(
    "Please install Java 17+ or Temurin JDK and ensure `java` is in your PATH.",
  );
  process.exit(1);
}

// Auto-build Spring Boot package if JAR file is missing
if (!fs.existsSync(jarPath)) {
  console.log(
    "\x1b[36m%s\x1b[0m",
    "📦 Packaging DevShell Spring Boot application...",
  );
  try {
    execSync("mvn package -DskipTests", { cwd: projectDir, stdio: "inherit" });
  } catch (e) {
    console.error(
      "\x1b[31m%s\x1b[0m",
      "✗ Build failed. Make sure Apache Maven (`mvn`) is installed.",
    );
    process.exit(1);
  }
}

// Execute Spring Boot JAR passing all command line arguments with explicit UTF-8 JVM flags
const args = [
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8",
  "-jar",
  jarPath,
  ...process.argv.slice(2),
];

const env = Object.assign({}, process.env);
delete env.JAVA_TOOL_OPTIONS; // Prevent JVM "Picked up JAVA_TOOL_OPTIONS" notice

const result = spawnSync("java", args, {
  stdio: "inherit",
  env: env,
  shell: true,
});

checkNpmUpdate();

process.exit(result.status !== null ? result.status : 0);

function checkNpmUpdate() {
  try {
    const https = require("https");
    const os = require("os");
    const pkg = require("../package.json");
    const currentVersion = pkg.version || "1.0.9";
    const cacheDir = path.join(os.homedir(), ".devshell");
    const cacheFile = path.join(cacheDir, "update.json");

    let cache = { latestVersion: currentVersion, lastChecked: 0 };
    if (fs.existsSync(cacheFile)) {
      try {
        cache = JSON.parse(fs.readFileSync(cacheFile, "utf8"));
      } catch (e) {}
    }

    if (isNewer(currentVersion, cache.latestVersion)) {
      console.log("");
      console.log(
        "\x1b[33m%s\x1b[0m",
        `  💡 Update available: ${currentVersion} → ${cache.latestVersion}`,
      );
      console.log(
        "\x1b[36m%s\x1b[0m",
        "  👉 Run `npm install -g devshell` to update to the latest version.\n",
      );
    }

    const FOUR_HOURS = 4 * 60 * 60 * 1000;
    if (Date.now() - (cache.lastChecked || 0) > FOUR_HOURS) {
      const req = https.get(
        "https://registry.npmjs.org/devshell/latest",
        { timeout: 1500 },
        (res) => {
          if (res.statusCode === 200) {
            let body = "";
            res.on("data", (chunk) => (body += chunk));
            res.on("end", () => {
              try {
                const data = JSON.parse(body);
                if (data && data.version) {
                  if (!fs.existsSync(cacheDir))
                    fs.mkdirSync(cacheDir, { recursive: true });
                  fs.writeFileSync(
                    cacheFile,
                    JSON.stringify({
                      latestVersion: data.version,
                      lastChecked: Date.now(),
                    }),
                  );
                }
              } catch (e) {}
            });
          }
        },
      );
      req.on("error", () => {});
    }
  } catch (e) {}
}

function isNewer(current, latest) {
  if (!current || !latest) return false;
  const cParts = current.split(".").map((n) => parseInt(n.replace(/[^0-9]/g, "")) || 0);
  const lParts = latest.split(".").map((n) => parseInt(n.replace(/[^0-9]/g, "")) || 0);
  const len = Math.max(cParts.length, lParts.length);
  for (let i = 0; i < len; i++) {
    const c = cParts[i] || 0;
    const l = lParts[i] || 0;
    if (l > c) return true;
    if (l < c) return false;
  }
  return false;
}
