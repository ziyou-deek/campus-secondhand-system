document.getElementById('form').addEventListener('submit', async (event) => {
  event.preventDefault()
  const message = document.getElementById('message')
  message.textContent = ''
  try {
    await window.desktopSetup.saveServerUrl(document.getElementById('url').value)
  } catch (error) {
    message.textContent = error.message || '连接失败，请检查网址'
  }
})
