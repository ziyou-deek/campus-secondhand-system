const { contextBridge, ipcRenderer } = require('electron')
contextBridge.exposeInMainWorld('desktopSetup', {
  saveServerUrl: (url) => ipcRenderer.invoke('save-server-url', url)
})
