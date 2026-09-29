const { test } = require('node:test')
const assert = require('node:assert/strict')
const { normalizeServerUrl } = require('./url.cjs')

test('accepts HTTPS origin and local development server', () => {
  assert.equal(normalizeServerUrl('https://trade.example.com/'), 'https://trade.example.com')
  assert.equal(normalizeServerUrl('http://localhost:5173/'), 'http://localhost:5173')
})

test('rejects insecure remote or URLs with credentials and paths', () => {
  assert.throws(() => normalizeServerUrl('http://121.43.101.242'), /HTTPS/)
  assert.throws(() => normalizeServerUrl('https://user:pass@example.com/'), /根地址/)
  assert.throws(() => normalizeServerUrl('https://example.com/admin'), /根地址/)
})
