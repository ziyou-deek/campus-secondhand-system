#!/usr/bin/env bash
set -euo pipefail

# 在专用目录部署校园二手平台；不修改现有 chat.service 和 80/443 站点。
APP_DIR=/opt/campus-secondhand
PUBLIC_IP=121.43.101.242
SITE=/etc/nginx/sites-available/campus-secondhand.conf
ENV_FILE=/etc/campus-secondhand.env
ADMIN_FILE=/root/campus-secondhand-initial-admin.txt

if [[ $(id -u) -ne 0 ]]; then echo 'Please run as root via sudo.' >&2; exit 1; fi
if [[ ! -f "$APP_DIR/backend/secondhand-1.0.0.jar" || ! -f "$APP_DIR/frontend/index.html" || ! -f "$APP_DIR/database/init.sql" ]]; then
  echo 'Missing uploaded backend, frontend or database script.' >&2; exit 1
fi

export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq mysql-server >/dev/null
systemctl enable --now mysql >/dev/null
if ! id campus-secondhand >/dev/null 2>&1; then
  useradd --system --home-dir "$APP_DIR" --shell /usr/sbin/nologin campus-secondhand
fi

if [[ ! -f "$ENV_FILE" ]]; then
  DB_PASSWORD=$(openssl rand -hex 24)
  JWT_SECRET=$(openssl rand -hex 48)
  ADMIN_PASSWORD=$(openssl rand -hex 18)
  mysql <<SQL
CREATE DATABASE IF NOT EXISTS campus_secondhand DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'campus_app'@'127.0.0.1' IDENTIFIED BY '$DB_PASSWORD';
GRANT ALL PRIVILEGES ON campus_secondhand.* TO 'campus_app'@'127.0.0.1';
FLUSH PRIVILEGES;
SQL
  if [[ $(mysql -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='campus_secondhand'") != 0 ]]; then
    echo 'Existing tables found; refusing to run destructive init.sql.' >&2; exit 1
  fi
  mysql --default-character-set=utf8mb4 < "$APP_DIR/database/init.sql"
  cat > "$ENV_FILE" <<ENV
DB_URL=jdbc:mysql://127.0.0.1:3306/campus_secondhand?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=campus_app
DB_PASSWORD=$DB_PASSWORD
JWT_SECRET=$JWT_SECRET
CORS_ORIGINS=http://localhost:5173,https://121.43.101.242:8443
INITIAL_ADMIN_USERNAME=campusadmin
INITIAL_ADMIN_PASSWORD=$ADMIN_PASSWORD
ENV
  chmod 600 "$ENV_FILE"
  printf 'username: campusadmin\npassword: %s\n' "$ADMIN_PASSWORD" > "$ADMIN_FILE"
  chmod 600 "$ADMIN_FILE"
fi
if ! grep -q '^UPLOAD_DIR=' "$ENV_FILE"; then
  printf 'UPLOAD_DIR=%s/uploads\n' "$APP_DIR" >> "$ENV_FILE"
fi

chown -R campus-secondhand:campus-secondhand "$APP_DIR/backend" "$APP_DIR/frontend"
mkdir -p "$APP_DIR/uploads/products" "$APP_DIR/uploads/avatars"
chown -R campus-secondhand:campus-secondhand "$APP_DIR/uploads"
cat > /etc/systemd/system/campus-secondhand.service <<UNIT
[Unit]
Description=Campus Secondhand Spring Boot API
After=network-online.target mysql.service
Wants=network-online.target
[Service]
Type=simple
User=campus-secondhand
WorkingDirectory=$APP_DIR/backend
EnvironmentFile=$ENV_FILE
ExecStart=/usr/bin/java -Xms128m -Xmx384m -jar $APP_DIR/backend/secondhand-1.0.0.jar --server.address=127.0.0.1
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
ProtectSystem=strict
ReadWritePaths=$APP_DIR/uploads
ProtectHome=true
PrivateTmp=true
[Install]
WantedBy=multi-user.target
UNIT

cat > "$SITE" <<NGINX
server {
    listen 8443 ssl;
    server_name $PUBLIC_IP;
    ssl_certificate /etc/letsencrypt/live/chat-ip-121-43-101-242/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/chat-ip-121-43-101-242/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    root $APP_DIR/frontend;
    index index.html;
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-Proto https;
    }
    location /uploads/ {
        alias $APP_DIR/uploads/;
        add_header X-Content-Type-Options nosniff always;
    }
    location / { try_files \$uri \$uri/ /index.html; }
}
NGINX
ln -sfn "$SITE" /etc/nginx/sites-enabled/campus-secondhand.conf
nginx -t
systemctl daemon-reload
systemctl enable --now campus-secondhand
systemctl reload nginx
for attempt in {1..20}; do
  if curl -fsS http://127.0.0.1:8080/api/products >/dev/null; then break; fi
  sleep 2
done
curl -fsS http://127.0.0.1:8080/api/products >/dev/null
# 管理员创建后，从 systemd 环境中移除明文初始密码；凭据只保留在 root 私有文件。
sed -i '/^INITIAL_ADMIN_/d' "$ENV_FILE"
systemctl restart campus-secondhand
echo 'Campus Secondhand installed on HTTPS port 8443.'
