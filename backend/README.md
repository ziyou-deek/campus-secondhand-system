# 后端启动

本服务使用 JDK 17、Spring Boot 2.7、MyBatis Plus 和 MySQL 8。数据库脚本位于项目根目录 `database/`。完整的本地运行、环境变量与管理员初始化说明请看根目录 [README](../README.md)。

Windows 在配置 `JAVA_HOME`、`DB_PASSWORD` 和至少 32 字节的 `JWT_SECRET` 后运行：

```powershell
.\mvnw.cmd test package
.\mvnw.cmd spring-boot:run
```

请勿把真实数据库密码或 JWT 密钥提交到仓库；`database/init.sql` 会删除旧表，只能用于新库。
