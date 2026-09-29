const { app, BrowserWindow, ipcMain, session, Menu } = require('electron')
if (require('electron-squirrel-startup')) app.quit()
app.setAppUserModelId('com.squirrel.CampusSecondhand.CampusSecondhand')
const fs = require('node:fs')
const path = require('node:path')
const { normalizeServerUrl } = require('./url.cjs')
const DEFAULT_SERVER_URL = 'https://121.43.101.242:8443'

let mainWindow
let setupWindow
const settingsPath = () => path.join(app.getPath('userData'), 'settings.json')

function savedUrl() {
  try { return normalizeServerUrl(JSON.parse(fs.readFileSync(settingsPath(), 'utf8')).serverUrl) }
  catch { return null }
}

function openPlatform(serverUrl) {
  if (setupWindow) { setupWindow.close(); setupWindow = null }
  if (mainWindow) mainWindow.close()
  mainWindow = new BrowserWindow({
    width: 1280, height: 820, minWidth: 900, minHeight: 650,
    webPreferences: { nodeIntegration: false, contextIsolation: true, sandbox: true }
  })
  const origin = new URL(serverUrl).origin
  mainWindow.webContents.setWindowOpenHandler(() => ({ action: 'deny' }))
  mainWindow.webContents.on('will-navigate', (event, target) => {
    try { if (new URL(target).origin !== origin) event.preventDefault() }
    catch { event.preventDefault() }
  })
  mainWindow.webContents.on('did-fail-load', (_event, code, _description, _url, isMainFrame) => {
    if (isMainFrame && code !== -3) openSetup()
  })
  mainWindow.loadURL(serverUrl)
}

function openSetup() {
  if (setupWindow) return
  setupWindow = new BrowserWindow({
    width: 550, height: 420, resizable: false,
    webPreferences: { preload: path.join(__dirname, 'preload.cjs'), nodeIntegration: false, contextIsolation: true, sandbox: true }
  })
  setupWindow.loadFile(path.join(__dirname, 'setup.html'))
}

app.whenReady().then(() => {
  Menu.setApplicationMenu(Menu.buildFromTemplate([
    { label: '平台', submenu: [
      { label: '重新连接', click: () => mainWindow?.reload() },
      { label: '更换平台地址', click: () => openSetup() },
      { role: 'quit', label: '退出' }
    ] }
  ]))
  session.defaultSession.setPermissionRequestHandler((_webContents, _permission, callback) => callback(false))
  ipcMain.handle('save-server-url', (event, value) => {
    if (event.sender !== setupWindow?.webContents) throw new Error('无权设置平台地址')
    const serverUrl = normalizeServerUrl(value)
    fs.writeFileSync(settingsPath(), JSON.stringify({ serverUrl }), { mode: 0o600 })
    openPlatform(serverUrl)
  })
  openPlatform(savedUrl() || DEFAULT_SERVER_URL)
})

app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit() })
