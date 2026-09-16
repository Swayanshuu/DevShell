const https = require('https');
const http = require('http');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');
const { verifyChecksum } = require('./verifier');

function drawProgressBar(percent, currentMB, totalMB) {
  const width = 20;
  const completed = Math.floor((percent / 100) * width);
  const remaining = width - completed;
  const bar = '█'.repeat(completed) + '░'.repeat(remaining);
  const info = totalMB ? `${currentMB.toFixed(1)}MB / ${totalMB.toFixed(1)}MB` : `${currentMB.toFixed(1)}MB`;
  process.stdout.write(`\r    [${bar}] ${percent.toFixed(0)}%  ${info}   `);
}

function downloadFile(url, destPath) {
  return new Promise((resolve, reject) => {
    const fileStream = fs.createWriteStream(destPath);
    
    function makeRequest(currentUrl, redirectDepth = 0) {
      if (redirectDepth > 10) {
        return reject(new Error('Too many redirects while downloading Java 21 runtime.'));
      }

      const client = currentUrl.startsWith('https') ? https : http;
      
      const req = client.get(currentUrl, { timeout: 30000 }, (res) => {
        if (res.statusCode >= 300 && res.statusCode < 400 && res.headers.location) {
          let redirectUrl = res.headers.location;
          if (redirectUrl.startsWith('/')) {
            const u = new URL(currentUrl);
            redirectUrl = `${u.protocol}//${u.host}${redirectUrl}`;
          }
          return makeRequest(redirectUrl, redirectDepth + 1);
        }

        if (res.statusCode !== 200) {
          return reject(new Error(`Server returned HTTP ${res.statusCode} ${res.statusMessage}`));
        }

        const totalBytes = parseInt(res.headers['content-length'], 10) || 0;
        let downloadedBytes = 0;

        res.on('data', (chunk) => {
          downloadedBytes += chunk.length;
          fileStream.write(chunk);

          if (totalBytes > 0) {
            const percent = (downloadedBytes / totalBytes) * 100;
            const currentMB = downloadedBytes / (1024 * 1024);
            const totalMB = totalBytes / (1024 * 1024);
            drawProgressBar(percent, currentMB, totalMB);
          } else {
            const currentMB = downloadedBytes / (1024 * 1024);
            drawProgressBar(100, currentMB, 0);
          }
        });

        res.on('end', () => {
          fileStream.end();
          process.stdout.write('\n');
          resolve();
        });

        res.on('error', (err) => {
          fileStream.close();
          fs.unlink(destPath, () => {});
          reject(err);
        });
      });

      req.on('error', (err) => {
        fileStream.close();
        fs.unlink(destPath, () => {});
        reject(err);
      });

      req.on('timeout', () => {
        req.destroy();
        fileStream.close();
        fs.unlink(destPath, () => {});
        reject(new Error('Network request timed out. Please check your internet connection.'));
      });
    }

    makeRequest(url);
  });
}

function extractArchive(archivePath, targetDir) {
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }

  try {
    // Try built-in tar executable available across Windows 10/11, macOS, and Linux
    execSync(`tar -xf "${archivePath}" -C "${targetDir}"`, { stdio: 'ignore' });
  } catch (err) {
    if (process.platform === 'win32' && archivePath.endsWith('.zip')) {
      // Fallback for Windows using PowerShell
      const command = `powershell -Command "Expand-Archive -Path '${archivePath}' -DestinationPath '${targetDir}' -Force"`;
      execSync(command, { stdio: 'ignore' });
    } else {
      throw new Error(`Failed to extract Java archive (${err.message}). Ensure tar or unzip is available.`);
    }
  }
}

async function downloadAndInstallRuntime(config, targetDir) {
  console.log('\x1b[36m%s\x1b[0m', '  DevShell Java 21 runtime not found.');
  console.log('\x1b[36m%s\x1b[0m', '  Setting up Java 21 runtime...');
  console.log('');

  const tempDir = path.join(path.dirname(targetDir), 'cache');
  if (!fs.existsSync(tempDir)) {
    fs.mkdirSync(tempDir, { recursive: true });
  }

  const filename = `java-21-runtime.${config.archiveType === 'zip' ? 'zip' : 'tar.gz'}`;
  const archivePath = path.join(tempDir, filename);

  try {
    await downloadFile(config.url, archivePath);

    // Verify SHA-256 Checksum
    process.stdout.write('  Verifying runtime integrity... ');
    const isValid = await verifyChecksum(archivePath, config.sha256);
    if (!isValid) {
      fs.unlinkSync(archivePath);
      console.log('\x1b[31m%s\x1b[0m', '[FAILED]');
      throw new Error('Runtime verification failed. Downloaded archive checksum did not match expected SHA-256.');
    }
    console.log('\x1b[32m%s\x1b[0m', '[OK]');

    // Extract runtime
    process.stdout.write('  Extracting Java 21 JRE... ');
    extractArchive(archivePath, targetDir);
    console.log('\x1b[32m%s\x1b[0m', '[OK]');

    // Clean up temporary archive file
    try {
      fs.unlinkSync(archivePath);
    } catch (e) {}

    console.log('');
    console.log('\x1b[32m%s\x1b[0m', '  ✓ Java runtime ready.');
    console.log('');
  } catch (err) {
    console.error('');
    console.error('\x1b[31m%s\x1b[0m', '  ✗ Unable to prepare DevShell Java runtime.');
    console.error(`  Reason: ${err.message}`);
    console.error('');
    console.error('  Please check your network connection and try again.');
    console.error('  Alternatively, install Java 21+ manually and ensure `java` is in your PATH.');
    console.error('');
    throw err;
  }
}

module.exports = {
  downloadFile,
  extractArchive,
  downloadAndInstallRuntime
};
