const { spawnSync } = require('child_process');
const crypto = require('crypto');
const fs = require('fs');
const { REQUIRED_JAVA_MAJOR } = require('./platform');

function parseJavaVersion(versionString) {
  if (!versionString) return null;
  
  // Match version strings like "21.0.4", "21", "22.0.1", "17.0.2", "1.8.0_311"
  const match = versionString.match(/(?:version\s*")?(\d+)(?:\.(\d+))?(?:\.(\d+))?/i);
  if (!match) return null;

  let major = parseInt(match[1], 10);
  // Handle legacy 1.8 notation
  if (major === 1 && match[2]) {
    major = parseInt(match[2], 10);
  }

  return major;
}

function verifyJavaExecutable(execPath) {
  if (!execPath) return { valid: false, version: null, isCompatible: false, error: 'Executable path not provided' };

  try {
    if (execPath !== 'java' && !fs.existsSync(execPath)) {
      return { valid: false, version: null, isCompatible: false, error: 'Executable file does not exist' };
    }

    const res = spawnSync(execPath, ['-version'], {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'pipe']
    });

    if (res.error || res.status !== 0) {
      return { valid: false, version: null, isCompatible: false, error: res.error ? res.error.message : `Process exited with code ${res.status}` };
    }

    const output = (res.stderr || '') + '\n' + (res.stdout || '');
    const major = parseJavaVersion(output);

    if (!major) {
      return { valid: false, version: null, isCompatible: false, error: 'Could not parse Java version from output' };
    }

    const isCompatible = major >= REQUIRED_JAVA_MAJOR;
    return {
      valid: true,
      version: major,
      isCompatible,
      output: output.trim()
    };
  } catch (err) {
    return { valid: false, version: null, isCompatible: false, error: err.message };
  }
}

function verifyChecksum(filePath, expectedSha256) {
  return new Promise((resolve, reject) => {
    if (!fs.existsSync(filePath)) {
      return resolve(false);
    }
    const hash = crypto.createHash('sha256');
    const stream = fs.createReadStream(filePath);

    stream.on('data', data => hash.update(data));
    stream.on('end', () => {
      const calculated = hash.digest('hex').toLowerCase();
      resolve(calculated === expectedSha256.toLowerCase());
    });
    stream.on('error', err => reject(err));
  });
}

module.exports = {
  parseJavaVersion,
  verifyJavaExecutable,
  verifyChecksum
};
