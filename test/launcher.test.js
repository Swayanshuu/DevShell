const assert = require('assert');
const path = require('path');
const fs = require('fs');
const { parseJavaVersion, verifyJavaExecutable } = require('../lib/launcher/verifier');
const { getPlatformKey, getRuntimeConfig, getManagedRuntimeDir, getManagedJavaExecutablePath } = require('../lib/launcher/platform');
const { resolveJavaRuntime } = require('../lib/launcher/runtime-manager');

console.log('🧪 Running DevShell Node Launcher Test Suite...\n');

let passedTests = 0;
let totalTests = 0;

function test(name, fn) {
  totalTests++;
  try {
    fn();
    console.log(`  ✓ PASS: ${name}`);
    passedTests++;
  } catch (err) {
    console.error(`  ✗ FAIL: ${name}`);
    console.error(`    ${err.message}`);
  }
}

async function asyncTest(name, fn) {
  totalTests++;
  try {
    await fn();
    console.log(`  ✓ PASS: ${name}`);
    passedTests++;
  } catch (err) {
    console.error(`  ✗ FAIL: ${name}`);
    console.error(`    ${err.message}`);
  }
}

// 1. Version Parsing Tests
test('Parse Java 21.0.4 version string', () => {
  const v = parseJavaVersion('openjdk version "21.0.4" 2024-07-16');
  assert.strictEqual(v, 21);
});

test('Parse Java 22 version string', () => {
  const v = parseJavaVersion('java version "22.0.1" 2024-04-16');
  assert.strictEqual(v, 22);
});

test('Parse Java 17 version string', () => {
  const v = parseJavaVersion('openjdk version "17.0.2" 2022-01-18');
  assert.strictEqual(v, 17);
});

test('Parse legacy Java 1.8 version string', () => {
  const v = parseJavaVersion('java version "1.8.0_311"');
  assert.strictEqual(v, 8);
});

test('Parse null or invalid string', () => {
  assert.strictEqual(parseJavaVersion(null), null);
  assert.strictEqual(parseJavaVersion('invalid text'), null);
});

// 2. Platform Config Tests
test('Detect current platform key', () => {
  const key = getPlatformKey();
  assert.ok(typeof key === 'string' && key.includes('-'));
});

test('Resolve runtime configuration for platform', () => {
  const config = getRuntimeConfig();
  assert.strictEqual(config.requiredJavaMajor, 21);
  assert.ok(config.url.startsWith('https://'));
  assert.ok(typeof config.sha256 === 'string' && config.sha256.length === 64);
});

// 3. System Java Verification Tests
test('Verify system Java executable', () => {
  const check = verifyJavaExecutable('java');
  assert.ok(typeof check.valid === 'boolean');
  if (check.valid) {
    assert.ok(typeof check.version === 'number');
    assert.strictEqual(check.isCompatible, check.version >= 21);
  }
});

// 4. Runtime Resolution Tests
(async () => {
  await asyncTest('Resolve Java runtime dynamically', async () => {
    const runtime = await resolveJavaRuntime();
    assert.ok(runtime.type === 'system' || runtime.type === 'managed');
    assert.ok(typeof runtime.execPath === 'string');
    assert.ok(runtime.version >= 21);
  });

  console.log(`\n📊 Test Summary: ${passedTests}/${totalTests} tests passed.`);
  if (passedTests !== totalTests) {
    process.exit(1);
  }
})();
