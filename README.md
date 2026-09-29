# 校园二手交易管理系统 1.0

基于用户提供的课程项目框架继续开发。学生可注册、登录、发布和编辑商品、上传商品图与头像、搜索商品、收藏、下单、评价及留言；管理员可管理用户、分类与商品。浏览器和 Windows 客户端连接同一台服务器，共享账户、商品与订单。Windows 客户端是平台入口，数据库和 Spring Boot 后端始终运行在服务器。

## 目录和环境

| 目录 | 用途 |
| --- | --- |
| `backend/` | Spring Boot 2.7、JDK 17、MyBatis Plus、JWT；含 `mvnw.cmd` |
| `frontend/` | Vue 3、Vite、Element Plus、Pinia、Axios |
| `desktop/` | Electron Forge Windows 安装包项目 |
| `database/` | 全新数据库建表、可选演示数据、旧版升级脚本 |
| `docs/` | 架构、部署和测试说明 |

开发环境：JDK 17、Node.js 20 或 24、npm、MySQL 8。Maven 可只用项目自带的 Wrapper。正式服务器建议 Ubuntu 24.04、JDK 17、MySQL 8、Nginx、HTTPS 域名。

## 首次本地运行（PowerShell）

1. 在 MySQL 终端交互登录，**仅对空的新库**执行 `database/init.sql`。该脚本会删除同名旧表，已有数据请改走备份和 `database/migrations/V1_0__order_reservation.sql`。`database/demo-data.sql` 是旧版本地样例，含固定弱密码，**不得用于公网正式环境**。当前市场演示数据使用 `database/seed-market-demo.py`。
2. 在当前 PowerShell 窗口设置后端环境变量（密码只在本机输入，不发到聊天）：

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:DB_USERNAME = 'root'
$pw = Read-Host 'MySQL 密码' -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($pw)
try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
$bytes = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
cd backend
.\mvnw.cmd spring-boot:run
```

3. 新开一个 PowerShell 窗口启动前端：

```powershell
cd frontend
npm ci
npm run dev
```

浏览器访问 `http://localhost:5173`；后端监听 `http://localhost:8080`，Vite 将 `/api` 和 `/uploads` 代理到后端。图片默认保存在 `backend/uploads/`；本地可通过 `UPLOAD_DIR` 指定其他目录。启动后可用 Postman 测试 `POST /api/auth/login`、`GET /api/products`、`GET /api/products/{id}`。统一返回格式为 `{ "code": 200, "msg": "success", "data": ... }`。

若需要在空库建立管理员，请在**首次启动**前另外设置 `INITIAL_ADMIN_USERNAME` 和不少于 12 字符的 `INITIAL_ADMIN_PASSWORD` 环境变量；创建后清除该变量。演示数据账号仅供本地：管理员 `admin/admin123`，学生 `student1/123456` 等。公网部署请使用独立强密码管理员。

## 构建与 Windows 安装包

```powershell
cd backend
.\mvnw.cmd test package
cd ..\frontend
npm ci
npm run build
cd ..\desktop
npm ci
npm test
npm run make
```

安装包生成在 `desktop/out/make/squirrel.windows/x64/`。首次启动默认连接已经部署的 `https://121.43.101.242:8443`；菜单“平台 → 更换平台地址”可改为其他 **HTTPS 根地址**，本机调试也允许 `http://localhost:5173`。所有用户连接同一个线上地址即可共享数据。安装包不包含后端、MySQL 或凭据；安装包未签名，Windows 可能显示未知发布者提示。

## 重要业务规则

- 商品发布后待审核，管理员审核通过才公开。待审核、下架、交易中或已售商品仅卖家及管理员可查看详情。
- 在“我的商品”可编辑未售且未进入交易的商品。修改后重新待审核；商品最多上传 4 张 JPG/PNG，每张不超过 5 MB，第一张为封面。个人中心可上传头像。图片保存在服务器，其他设备连接同一平台即可访问。
- 已上传的商品封面也显示在订单列表和订单详情；聊天会话显示对方头像。
- 商品在售时只有一位买家能抢占并创建订单；取消订单会恢复在售。卖家开始交易后，买卖双方各自确认，双方确认后订单完成，商品变为已售。
- 禁用账号的旧 JWT 不能继续访问需登录接口。
- 用户忘记密码时，可线下联系管理员核实身份。管理员在“用户管理”点击“重置密码”，复制只显示一次的随机临时密码并私下交给用户；用户登录后在个人中心修改密码。重置或修改密码后，该用户原有登录状态失效，需重新登录。管理员账户不可通过此功能重置；暂不支持短信或邮件自助找回。

## 服务器部署与课程文档

线上演示地址：[https://121.43.101.242:8443/](https://121.43.101.242:8443/)（浏览器与桌面客户端共用）。服务器使用独立的 `campus-secondhand.service`、本机 MySQL 数据库和 Nginx 8443 站点；原聊天服务仍使用 443。管理员凭据只保存在服务器 root 可读的 `/root/campus-secondhand-initial-admin.txt`，请从**你自己的终端**通过 SSH 查看，勿将密码发到聊天。SSH 登录用户是镜像要求的 `ecs-user`，而非 root。首次登录后建议在个人中心修改密码。

部署流程见 [docs/deployment.md](docs/deployment.md)，本轮验证与已知限制见 [docs/test-report.md](docs/test-report.md)。原始框架来源、已完成模块和代码沿用范围见 [docs/origin.md](docs/origin.md)。

校园二手市场演示数据（10 个分类、5 个模拟卖家、10 件商品与图片授权）见 [docs/demo-market.md](docs/demo-market.md)。图片和账号由 `database/seed-market-demo.py` 导入，重复运行不会重复发布；线上已经导入。演示商品标题带“【演示】”，图片与价格仅供课程展示。
