function normalizeServerUrl(value) {
  if (typeof value !== 'string') throw new Error('请输入平台地址')
  let url
  try { url = new URL(value.trim()) } catch { throw new Error('平台地址格式不正确') }
  const local = ['localhost', '127.0.0.1'].includes(url.hostname)
  if (url.protocol !== 'https:' && !(url.protocol === 'http:' && local)) {
    throw new Error('远程平台必须使用 HTTPS')
  }
  if (url.username || url.password || url.search || url.hash || url.pathname !== '/') {
    throw new Error('请输入平台根地址，例如 https://example.com')
  }
  return url.origin
}
module.exports = { normalizeServerUrl }
