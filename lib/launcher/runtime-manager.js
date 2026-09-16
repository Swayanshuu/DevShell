const fs = require('fs');
const {
  getRuntimeConfig,
  getManagedRuntimeDir,
  getManagedJavaExecutablePath,
  REQUIRED_JAVA_MAJOR
} = require('./platform');
const { verifyJavaExecutable } = require('./verifier');
const { downloadAndInstallRuntime } = require('./downloader');

async function resolveJavaRuntime() {
  const isDevMode = process.env.DEVSHELL_DEV_MODE === 'true';

  // 1. Check System Java
  const sysCheck = verifyJavaExecutable('java');
  if (sysCheck.valid && sysCheck.isCompatible) {
    return {
      type: 'system',
      execPath: 'java',
      version: sysCheck.version
    };
  }

  if (isDevMode && !sysCheck.isCompatible) {
    console.error('\x1b[31m%s\x1b[0m', `✗ DEVSHELL_DEV_MODE is enabled, but system Java is ${sysCheck.valid ? `v${sysCheck.version} (Java 21+ required)` : 'missing'}.`);
    process.exit(1);
  }

  // 2. Check Managed Runtime
  const managedExecPath = getManagedJavaExecutablePath();
  const managedCheck = verifyJavaExecutable(managedExecPath);

  if (managedCheck.valid && managedCheck.isCompatible) {
    return {
      type: 'managed',
      execPath: managedExecPath,
      version: managedCheck.version
    };
  }

  // 3. Download & Install Managed Runtime if missing or corrupted
  const config = getRuntimeConfig();
  const targetDir = getManagedRuntimeDir();

  await downloadAndInstallRuntime(config, targetDir);

  const postInstallCheck = verifyJavaExecutable(managedExecPath);
  if (!postInstallCheck.valid || !postInstallCheck.isCompatible) {
    throw new Error('Downloaded Java runtime is corrupt or incompatible.');
  }

  return {
    type: 'managed',
    execPath: managedExecPath,
    version: postInstallCheck.version
  };
}

module.exports = {
  resolveJavaRuntime
};
