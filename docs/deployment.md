# Ubuntu 24.04 部署说明

实际架构：原聊天服务继续使用 Nginx 80/443 和 Java 8082；校园二手系统独立使用 Nginx HTTPS 8443，并将 `/api/` 转发到仅监听本机的 Spring Boot 8080。MySQL 仅监听本机 3306。Windows 客户端加载 `https://121.43.101.242:8443`。2 核 2 GiB 的课程演示服务器采用单机部署，不额外安装 Redis、消息队列等服务。

SSH 主机指纹已与用户在阿里云控制台读取的 SHA256 指纹核对。安全组已开放 TCP 8443；公网访问与证书校验已验证。不要在聊天中发送 SSH 私钥、MySQL 密码或 JWT 密钥。

已执行的部署顺序：

1. 核对 SSH 主机指纹，登录后检查系统、内存、磁盘以及 JDK/MySQL/Nginx 状态。
2. 安装 Ubuntu 的 OpenJDK 17、MySQL 8、Nginx；创建独立系统用户和仅访问 `campus_secondhand` 的数据库用户。MySQL 不开放公网端口。
3. 在新库导入 `database/init.sql`，**不导入 `demo-data.sql`**；已有旧库先备份，再仅执行迁移脚本。
4. 将 `backend/target/secondhand-1.0.0.jar` 与 `frontend/dist/` 上传。用 root 可读的独立环境文件保存 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`，文件权限 600。首次启动后的管理员初始化变量已删除；初始账号信息保存在 root 私有文件中。
5. systemd 启动后端，设置 `ExecStart=/usr/bin/java -Xms128m -Xmx512m -jar /opt/campus-secondhand/backend/secondhand-1.0.0.jar --server.address=127.0.0.1`，`EnvironmentFile=` 指向本机私密文件。
6. Nginx 的新站点监听 8443，`root` 指向独立的前端目录，SPA 路由使用 `try_files $uri $uri/ /index.html`；`location /api/` 代理 `http://127.0.0.1:8080`。`/uploads/` 从独立图片目录读取，后端环境变量 `UPLOAD_DIR=/opt/campus-secondhand/uploads`，systemd 仅放行该目录写入。复用现有覆盖公网 IP 的受信任证书和续期机制。安全组开放 8443；22 建议限制到管理 IP。
7. 从另一台设备用浏览器注册测试账号，验证商品发布/审核/下单/取消/双方确认。桌面安装包填写同一 HTTPS 根地址，再复验共享数据。

实际独立 Nginx 站点配置见 `deploy/install-ubuntu.sh`；以下为简化示意：

```nginx
server {
    listen 8443 ssl;
    server_name 121.43.101.242;
    ssl_certificate /etc/letsencrypt/live/chat-ip-121-43-101-242/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/chat-ip-121-43-101-242/privkey.pem;
    root /opt/campus-secondhand/frontend;
    index index.html;
    location /api/ { proxy_pass http://127.0.0.1:8080; proxy_set_header Host $host; proxy_set_header X-Forwarded-Proto $scheme; }
    location /uploads/ { alias /opt/campus-secondhand/uploads/; add_header X-Content-Type-Options nosniff always; }
    location / { try_files $uri $uri/ /index.html; }
}
```

备份建议：每日 `mysqldump --single-transaction` 备份业务库，同时备份 `/opt/campus-secondhand/uploads/` 图片目录，并定期下载到独立存储；升级前再做一次手动备份。SQL 初始化脚本含 DROP，仅用于新库，不可对已有库重复执行。

本轮演示数据导入前的数据库备份保存在服务器 `/opt/campus-secondhand/backups/20260928-before-market-seed.sql`（root 私有）。演示卖家凭据保存在 `/root/campus-secondhand-demo-sellers.json`（权限 600）。导入步骤及图片出处见 [demo-market.md](demo-market.md)。
