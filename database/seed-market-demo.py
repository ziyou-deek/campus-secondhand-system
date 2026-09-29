"""为已部署平台补充校园二手市场演示数据。运行前请阅读 docs/demo-market.md。

只创建缺失的演示分类、卖家和商品；不删除现有用户、订单、商品。
默认在服务器上以 root 运行，管理员凭据和新卖家密码始终保存在 root 私有文件中。
"""

import argparse
import json
import os
import secrets
import urllib.error
import urllib.request
from pathlib import Path

CATEGORIES = [
    ("电脑办公", 10), ("手机数码", 20), ("图书教材", 30), ("家用电器", 40),
    ("家居日用", 50), ("服饰鞋包", 60), ("运动户外", 70),
    ("交通出行", 80), ("乐器文娱", 90), ("其他闲置", 99),
]
SELLERS = {
    "demo_seller_digital": "演示卖家·数码",
    "demo_seller_books": "演示卖家·图书",
    "demo_seller_life": "演示卖家·生活",
    "demo_seller_sport": "演示卖家·运动",
    "demo_seller_style": "演示卖家·箱包",
}
PRODUCTS = [
    ("【演示】游戏笔记本电脑", "电脑办公", 1650, "八成新", "laptop", "demo_seller_digital"),
    ("【演示】平板电脑与键盘套装", "手机数码", 860, "九成新", "tablet", "demo_seller_digital"),
    ("【演示】头戴式有线耳机", "手机数码", 75, "八成新", "headphones", "demo_seller_digital"),
    ("【演示】中文阅读书籍一组", "图书教材", 48, "八成新", "books", "demo_seller_books"),
    ("【演示】宿舍桌面台灯", "家居日用", 28, "七成新", "lamp", "demo_seller_life"),
    ("【演示】电热水壶", "家用电器", 35, "八成新", "kettle", "demo_seller_life"),
    ("【演示】黑色大容量双肩包", "服饰鞋包", 65, "九成新", "backpack", "demo_seller_style"),
    ("【演示】羽毛球拍一支", "运动户外", 55, "九成新", "badminton", "demo_seller_sport"),
    ("【演示】通勤自行车", "交通出行", 280, "七成新", "bicycle", "demo_seller_sport"),
    ("【演示】原木色民谣吉他", "乐器文娱", 360, "八成新", "guitar", "demo_seller_sport"),
]

def call(base, method, path, data=None, token=None, content_type="application/json"):
    headers = {"Content-Type": content_type}
    if token:
        headers["Authorization"] = "Bearer " + token
    payload = data if isinstance(data, bytes) else (None if data is None else json.dumps(data, ensure_ascii=False).encode("utf-8"))
    req = urllib.request.Request(base + path, data=payload, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as response:
            raw = response.read()
    except urllib.error.HTTPError as error:
        raw = error.read()
    result = json.loads(raw)
    if result.get("code") != 200:
        raise RuntimeError(f"{method} {path}: {result.get('code')} {result.get('msg')}")
    return result.get("data")

def read_admin(path):
    lines = path.read_text(encoding="utf-8").splitlines()
    values = dict(line.split(": ", 1) for line in lines if ": " in line)
    if not values.get("username") or not values.get("password"):
        raise RuntimeError("管理员凭据文件缺少 username/password")
    return values

def save_private(path, values):
    path.parent.mkdir(parents=True, exist_ok=True)
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    with os.fdopen(fd, "w", encoding="utf-8") as stream:
        json.dump(values, stream, ensure_ascii=False, indent=2)
    os.chmod(path, 0o600)

def upload(base, token, filename, image):
    boundary = "campusdemo" + secrets.token_hex(12)
    body = (f"--{boundary}\r\nContent-Disposition: form-data; name=\"kind\"\r\n\r\nproducts\r\n"
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; filename=\"{filename}\"\r\n"
            f"Content-Type: image/jpeg\r\n\r\n").encode() + image + f"\r\n--{boundary}--\r\n".encode()
    return call(base, "POST", "/api/uploads/images", body, token, "multipart/form-data; boundary=" + boundary)["url"]

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://127.0.0.1:8080")
    parser.add_argument("--admin-file", type=Path, default=Path("/root/campus-secondhand-initial-admin.txt"))
    parser.add_argument("--seller-file", type=Path, default=Path("/root/campus-secondhand-demo-sellers.json"))
    args = parser.parse_args()
    base = args.base.rstrip("/")
    images_dir = Path(__file__).resolve().parent / "demo-images"
    manifest = json.loads((images_dir / "manifest.json").read_text(encoding="utf-8"))
    admin = read_admin(args.admin_file)
    admin_token = call(base, "POST", "/api/auth/login", admin)["token"]

    all_categories = call(base, "GET", "/api/categories")
    categories = {c["name"]: c for c in all_categories}
    # 首版联调分类已承载旧商品，保留其 ID 并改为通用分类，避免孤立历史记录。
    legacy = [c for c in all_categories if c["name"] == "联调测试分类"]
    for old, target, sort in zip(sorted(legacy, key=lambda c: c["id"]), ("其他闲置", "图书教材"), (99, 30)):
        if target not in categories:
            changed = call(base, "PUT", f"/api/admin/categories/{old['id']}", {"name": target, "sort": sort}, admin_token)
            categories.pop(old["name"], None)
            categories[target] = changed
    for name, sort in CATEGORIES:
        if name not in categories:
            categories[name] = call(base, "POST", "/api/admin/categories", {"name": name, "sort": sort}, admin_token)
        elif categories[name].get("sort") != sort:
            categories[name] = call(base, "PUT", f"/api/admin/categories/{categories[name]['id']}", {"sort": sort}, admin_token)

    stored = json.loads(args.seller_file.read_text(encoding="utf-8")) if args.seller_file.exists() else {}
    existing = {u["username"] for u in call(base, "GET", "/api/admin/users?size=100", token=admin_token)["records"]}
    seller_tokens = {}
    for username, nickname in SELLERS.items():
        if username not in stored:
            if username in existing:
                raise RuntimeError(f"{username} 已存在但没有本轮的凭据文件；为避免接管账号，已停止")
            password = secrets.token_urlsafe(22)
            call(base, "POST", "/api/auth/register", {"username": username, "password": password, "nickname": nickname})
            stored[username] = password
            save_private(args.seller_file, stored)
        seller_tokens[username] = call(base, "POST", "/api/auth/login", {"username": username, "password": stored[username]})["token"]

    counts = {"created": 0, "already_present": 0}
    for title, category, price, condition, photo_key, seller in PRODUCTS:
        token = seller_tokens[seller]
        mine = call(base, "GET", "/api/products/mine?size=100", token=token)["records"]
        found = next((p for p in mine if p["title"] == title), None)
        if found:
            counts["already_present"] += 1
            if found["status"] == 0:
                call(base, "PUT", f"/api/admin/products/{found['id']}/audit", {"status": 1}, admin_token)
            continue
        meta = manifest[photo_key]
        image_url = upload(base, token, meta["file"], (images_dir / meta["file"]).read_bytes())
        description = (
            "课程演示商品，非真实售卖；规格、成色和价格均为模拟数据。图片为同类实物示意，不代表实际交付物。"
            f"\n图片：{meta['author']} / Wikimedia Commons；{meta['license']}；"
            f"来源：{meta['source']}；许可：{meta['license_url'] or meta['source']}。"
            "使用 Commons 缩略图，未做其他修改。"
        )
        product = call(base, "POST", "/api/products", {
            "title": title, "categoryId": categories[category]["id"], "price": price,
            "conditionLevel": condition, "description": description, "images": image_url,
        }, token)
        call(base, "PUT", f"/api/admin/products/{product['id']}/audit", {"status": 1}, admin_token)
        counts["created"] += 1
    print(f"演示分类 {len(CATEGORIES)} 类；新增商品 {counts['created']} 件；已有商品 {counts['already_present']} 件。")
    print(f"演示卖家凭据保存在 {args.seller_file}，此处不输出密码。")

if __name__ == "__main__":
    main()
