const { execSync } = require('child_process');
const path = require('path');
const pkg = require('../../package.json');
const { getPlatformKey, getDevShellDir } = require('./platform');

function getNpmVersion() {
  try {
    return execSync('npm -v', { encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
  } catch (e) {
    return 'Unknown';
  }
}

function runDiagnostics(runtimeResolution) {
  const platformKey = getPlatformKey();
  const npmVersion = getNpmVersion();
  const devshellDir = getDevShellDir();

  const isManaged = runtimeResolution.type === 'managed';
  const javaDesc = isManaged
    ? `DevShell Managed Runtime ${runtimeResolution.version}`
    : `System Java ${runtimeResolution.version}`;

  console.log('');
  console.log('\x1b[36m%s\x1b[0m', 'DEVSHELL ENVIRONMENT DIAGNOSTICS');
  console.log('────────────────────────────────────────');
  console.log(`  DevShell       v${pkg.version || '1.0.15'}`);
  console.log(`  Java           ${javaDesc}`);
  console.log(`  Java Binary    ${runtimeResolution.execPath}`);
  console.log(`  Node.js        ${process.version}`);
  console.log(`  NPM            v${npmVersion}`);
  console.log(`  Platform       ${platformKey}`);
  console.log(`  Storage        ${devshellDir}`);
  console.log('────────────────────────────────────────');
  console.log('\x1b[32m%s\x1b[0m', '  ✓ Environment ready.');
  console.log('');
}

module.exports = {
  runDiagnostics
};
