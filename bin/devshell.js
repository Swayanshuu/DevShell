#!/usr/bin/env node

const { spawnSync, execSync } = require('child_process');
const path = require('path');
const fs = require('fs');

// Ensure Windows Console uses UTF-8 Code Page 65001
if (process.platform === 'win32') {
  try {
    execSync('chcp 65001', { stdio: 'ignore' });
  } catch (e) {}
}

const jarPath = path.join(__dirname, '..', 'target', 'devshell-1.0.8.jar');
const projectDir = path.join(__dirname, '..');

// Check if Java runtime is available
try {
  execSync('java -version', { stdio: 'ignore' });
} catch (e) {
  console.error('\x1b[31m%s\x1b[0m', '✗ Error: Java 17+ JRE/JDK is required to run DevShell.');
  console.error('Please install Java 17+ or Temurin JDK and ensure `java` is in your PATH.');
  process.exit(1);
}

// Auto-build Spring Boot package if JAR file is missing
if (!fs.existsSync(jarPath)) {
  console.log('\x1b[36m%s\x1b[0m', '📦 Packaging DevShell Spring Boot application...');
  try {
    execSync('mvn package -DskipTests', { cwd: projectDir, stdio: 'inherit' });
  } catch (e) {
    console.error('\x1b[31m%s\x1b[0m', '✗ Build failed. Make sure Apache Maven (`mvn`) is installed.');
    process.exit(1);
  }
}

// Execute Spring Boot JAR passing all command line arguments with explicit UTF-8 JVM flags
const args = [
  '-Dfile.encoding=UTF-8',
  '-Dsun.stdout.encoding=UTF-8',
  '-Dsun.stderr.encoding=UTF-8',
  '-jar',
  jarPath,
  ...process.argv.slice(2)
];

const env = Object.assign({}, process.env);
delete env.JAVA_TOOL_OPTIONS; // Prevent JVM "Picked up JAVA_TOOL_OPTIONS" notice

const result = spawnSync('java', args, {
  stdio: 'inherit',
  env: env,
  shell: true
});

process.exit(result.status !== null ? result.status : 0);
