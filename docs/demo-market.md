# 校园二手市场演示数据与图片来源

本轮向现有平台补充 10 个常见分类和 10 件商品，由 5 个专用演示账号发布。商品标题统一含“【演示】”，详情写明图片为同类实物示意、商品并非真实售卖。真实用户发布的商品、账号和订单不会被覆盖。分类采用一级分类，适合课程项目规模：电脑办公、手机数码、图书教材、家用电器、家居日用、服饰鞋包、运动户外、交通出行、乐器文娱、其他闲置。旧“联调测试分类”的两个 ID 会改名为“其他闲置”和“图书教材”，以保留历史记录的分类关联。

服务器上先备份业务库，再将 `database/seed-market-demo.py` 与 `database/demo-images/` 一起放到同一目录，在服务器执行：

```bash
sudo python3 database/seed-market-demo.py
```

脚本通过本机 API 注册演示卖家、上传图片、发布商品并用管理员账号审核。管理员凭据默认从 root 私有文件 `/root/campus-secondhand-initial-admin.txt` 读取，不向终端输出；新卖家密码写入 `/root/campus-secondhand-demo-sellers.json`，权限 600。需要登录演示卖家时，由服务器管理员在自己的终端读取该文件，勿把密码发到公开聊天。重复运行脚本会跳过已有演示商品。

图片来自 Wikimedia Commons 文件页，使用其提供的缩略图，不做其他图像修改。不同文件的许可条件不同，下面逐张列出作者和许可；商品详情也包含对应署名、来源页及许可链接。照片只用于课程演示商品的视觉示意，不能证明商品的真实成色、品牌或卖家持有情况。再次发布或另作他用时，请按各文件页的许可条件保留署名。Wikimedia Commons 的[站外复用说明](https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia)强调应逐文件核对授权及署名要求。

| 图片文件 | 来源 | 作者/署名 | 许可 |
| --- | --- | --- | --- |
| laptop | [File:Laptop image.jpg](https://commons.wikimedia.org/wiki/File:Laptop_image.jpg) | Darakshanehaluddin | [CC0](http://creativecommons.org/publicdomain/zero/1.0/deed.en) |
| tablet | [File:Tablet computer.jpg](https://commons.wikimedia.org/wiki/File:Tablet_computer.jpg) | KK IN HK | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) |
| headphones | [File:Headphones on desk.jpg](https://commons.wikimedia.org/wiki/File:Headphones_on_desk.jpg) | Lirazelf | [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0) |
| books | [File:2006-12-18 A stack of books, from Taiwan.jpg](https://commons.wikimedia.org/wiki/File:2006-12-18_A_stack_of_books,_from_Taiwan.jpg) | Cheon Fong Liew | [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0) |
| lamp | [File:A desk lamp.jpg](https://commons.wikimedia.org/wiki/File:A_desk_lamp.jpg) | AirbusA330772673 | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) |
| kettle | [File:Electric kettle phillips white.JPG](https://commons.wikimedia.org/wiki/File:Electric_kettle_phillips_white.JPG) | Rafa | Public domain |
| backpack | [File:Wikimedia swag backpack.jpg](https://commons.wikimedia.org/wiki/File:Wikimedia_swag_backpack.jpg) | Kaartic | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) |
| badminton | [File:Badminton Racket.jpg](https://commons.wikimedia.org/wiki/File:Badminton_Racket.jpg) | hvshop | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) |
| bicycle | [File:Parked bicycle (52372051558).jpg](https://commons.wikimedia.org/wiki/File:Parked_bicycle_(52372051558).jpg) | Sergiy Galyonkin from Raleigh, USA | [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0) |
| guitar | [File:00 Walden Acoustic Guitar D310e 01.jpg](https://commons.wikimedia.org/wiki/File:00_Walden_Acoustic_Guitar_D310e_01.jpg) | Elmschrat | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) |
