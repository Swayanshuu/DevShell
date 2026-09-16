const os = require('os');
const path = require('path');

const REQUIRED_JAVA_MAJOR = 21;

const RUNTIME_PACKAGES = {
  'win32-x64': {
    url: 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_windows_hotspot_21.0.4_7.zip',
    sha256: 'a2e58c0df1b8b85750438ec67ea043c2206ef45ce48705f4e7c7a5fa016bbbf9',
    archiveType: 'zip',
    binRelativePath: path.join('jdk-21.0.4+7-jre', 'bin', 'java.exe'),
  },
  'linux-x64': {
    url: 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_linux_hotspot_21.0.4_7.tar.gz',
    sha256: '38a16dbd783d7398188166d3a9582d1c6e4e5cf4817a15a818ec06f0f5b820a4',
    archiveType: 'tar.gz',
    binRelativePath: path.join('jdk-21.0.4+7-jre', 'bin', 'java'),
  },
  'darwin-x64': {
    url: 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_x64_mac_hotspot_21.0.4_7.tar.gz',
    sha256: '9b05770c8a6f3b063d86dd1c89018617d12f32f33c373b98ea15c5443372c3d5',
    archiveType: 'tar.gz',
    binRelativePath: path.join('jdk-21.0.4+7-jre', 'Contents', 'Home', 'bin', 'java'),
  },
  'darwin-arm64': {
    url: 'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jre_aarch64_mac_hotspot_21.0.4_7.tar.gz',
    sha256: 'bc9e5c46e0e0e0a5ffdd7715fbc5b34a66a1e7b99c8f0015504ef53a1525c3ec',
    archiveType: 'tar.gz',
    binRelativePath: path.join('jdk-21.0.4+7-jre', 'Contents', 'Home', 'bin', 'java'),
  }
};

function getPlatformKey() {
  const platform = process.platform;
  const arch = process.arch;
  return `${platform}-${arch}`;
}

function getRuntimeConfig() {
  const key = getPlatformKey();
  const config = RUNTIME_PACKAGES[key];
  if (!config) {
    throw new Error(`Unsupported platform/architecture: ${key}. DevShell requires Windows x64, Linux x64, or macOS (x64/ARM64).`);
  }
  return {
    platformKey: key,
    requiredJavaMajor: REQUIRED_JAVA_MAJOR,
    ...config
  };
}

function getDevShellDir() {
  return path.join(os.homedir(), '.devshell');
}

function getManagedRuntimeDir() {
  return path.join(getDevShellDir(), 'runtime', `java-${REQUIRED_JAVA_MAJOR}`);
}

function getManagedJavaExecutablePath() {
  const config = getRuntimeConfig();
  const baseDir = getManagedRuntimeDir();
  return path.join(baseDir, config.binRelativePath);
}

module.exports = {
  REQUIRED_JAVA_MAJOR,
  getPlatformKey,
  getRuntimeConfig,
  getDevShellDir,
  getManagedRuntimeDir,
  getManagedJavaExecutablePath
};
