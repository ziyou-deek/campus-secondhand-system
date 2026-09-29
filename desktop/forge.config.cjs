module.exports = {
  packagerConfig: {
    asar: true,
    executableName: 'CampusSecondhand',
    win32metadata: { CompanyName: 'Campus Secondhand Project', FileDescription: '校园二手交易平台' }
  },
  makers: [{ name: '@electron-forge/maker-squirrel', config: { name: 'CampusSecondhand' } }]
}
