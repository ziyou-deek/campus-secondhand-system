"""在已部署的服务器上运行：sudo python3 /opt/campus-secondhand/deploy/smoke-server.py。

使用自动生成的测试账号和商品验证完整交易流程，不读取或输出任何密钥。
"""

import json
import secrets
import urllib.error
import urllib.request
from pathlib import Path

BASE = "http://127.0.0.1:8080"


def api(method, path, body=None, token=None):
    payload = None if body is None else json.dumps(body).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    request = urllib.request.Request(BASE + path, data=payload, headers=headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        return json.load(error)


def ok(method, path, body=None, token=None):
    result = api(method, path, body, token)
    if result.get("code") != 200:
        raise AssertionError(f"{method} {path}: {result.get('code')} {result.get('msg')}")
    return result.get("data")


def register_login(prefix):
    username = prefix + secrets.token_hex(4)
    password = secrets.token_urlsafe(16)
    ok("POST", "/api/auth/register", {"username": username, "password": password})
    return ok("POST", "/api/auth/login", {"username": username, "password": password})["token"]


def main():
    credentials = dict(line.strip().split(": ", 1)
                       for line in Path("/root/campus-secondhand-initial-admin.txt").read_text().splitlines())
    admin = ok("POST", "/api/auth/login", credentials)["token"]
    seller, buyer, other = (register_login(prefix) for prefix in ("seller_", "buyer_", "other_"))
    category = ok("POST", "/api/admin/categories", {"name": "联调测试分类", "sort": 99}, admin)

    def publish(title):
        product = ok("POST", "/api/products", {
            "title": title, "description": "自动化联调商品", "price": 18.5,
            "categoryId": category["id"], "conditionLevel": "九成新"
        }, seller)
        assert api("GET", f"/api/products/{product['id']}")["code"] == 403
        ok("PUT", f"/api/admin/products/{product['id']}/audit", {"status": 1}, admin)
        return product["id"]

    product_id = publish("联调：双方确认完成")
    order = ok("POST", "/api/orders", {"productId": product_id}, buyer)
    assert api("POST", "/api/orders", {"productId": product_id}, other)["code"] != 200
    ok("PUT", f"/api/orders/{order['id']}/status", {"status": 1}, seller)
    once = ok("PUT", f"/api/orders/{order['id']}/status", {"status": 2}, buyer)
    assert once["status"] == 1 and once["buyerConfirmed"] is True
    done = ok("PUT", f"/api/orders/{order['id']}/status", {"status": 2}, seller)
    assert done["status"] == 2 and done["sellerConfirmed"] is True

    second_id = publish("联调：取消后重新下单")
    first = ok("POST", "/api/orders", {"productId": second_id}, buyer)
    ok("PUT", f"/api/orders/{first['id']}/status", {"status": 3}, buyer)
    second = ok("POST", "/api/orders", {"productId": second_id}, other)
    assert second["status"] == 0

    username = "password_" + secrets.token_hex(4)
    old_password = secrets.token_urlsafe(16)
    new_password = secrets.token_urlsafe(16)
    ok("POST", "/api/auth/register", {"username": username, "password": old_password})
    token = ok("POST", "/api/auth/login", {"username": username, "password": old_password})["token"]
    ok("PUT", "/api/auth/profile", {"password": new_password}, token)
    assert ok("POST", "/api/auth/login", {"username": username, "password": old_password})
    assert api("PUT", "/api/auth/password", {"oldPassword": "wrong", "newPassword": new_password}, token)["code"] != 200
    ok("PUT", "/api/auth/password", {"oldPassword": old_password, "newPassword": new_password}, token)
    assert api("POST", "/api/auth/login", {"username": username, "password": old_password})["code"] != 200
    assert ok("POST", "/api/auth/login", {"username": username, "password": new_password})
    print("PASS: 注册登录、审核与可见性、独占下单、双方确认、取消重下单、密码修改")


if __name__ == "__main__":
    main()
