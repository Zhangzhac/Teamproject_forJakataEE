# JSP 聊天室

基于 Jakarta EE 10 的多人聊天室，使用 Servlet、JSP/JSTL 和 H2。界面支持中文和英文，适配桌面与手机。已在 **Apache TomEE 10.3.0 Web Profile + JDK 21** 上验证，构建目标为 Java 17。

## 快速开始

1. 安装 **JDK 17 或更高版本**，配置 `JAVA_HOME`，并下载、解压 [TomEE 10 Web Profile](https://tomee.apache.org/download.html)。
2. 从仓库根目录进入 `Project/jspchatroom`，按系统执行构建命令。Maven Wrapper 首次运行需要联网下载依赖。
3. 将生成的 `target/jspchatroom.war` 复制到 TomEE 的 `webapps/` 目录。在 TomEE 目录运行 `bin\catalina.bat run`（Windows）或 `sh bin/catalina.sh run`（Linux/macOS）。
4. 打开 [聊天室](http://localhost:8080/jspchatroom/)，注册账号后登录。若修改过 HTTP 端口，请使用实际端口。

**Windows PowerShell**

```powershell
cd Project/jspchatroom
.\mvnw.cmd clean package
```

**Linux / macOS**

```bash
cd Project/jspchatroom
sh ./mvnw clean package
```

WAR 内含 H2 驱动，Servlet、JSP 和 JSTL 由 TomEE 提供。升级已有部署时，先停止 TomEE 并备份数据库，再替换 `jspchatroom.war`。如果 TomEE 仍使用旧的解压目录或 JSP 编译缓存，在重启前移出 `webapps/jspchatroom/` 和 `work/Catalina/localhost/jspchatroom/`。

## 使用功能

- **聊天**：按 **Enter** 发送，按 **Shift + Enter** 换行。中文输入法确认候选词时不会触发发送。发送期间按钮会变淡并显示「发送中…」。消息和在线用户列表约每两秒更新一次，草稿与已展开的译文会保留。
- **主题**：在聊天区右上角选择「默认」「全能编程大师」或「我们联合」。主题保存在当前浏览器，刷新、切换语言或打开设置页后仍会生效。
- **头像**：在「设置 → 个人头像」选择 JPEG 或 PNG（最大 5 MB、2400 万像素），确认圆形预览后点击「保存头像」。图片会居中裁切并压缩为 **200 × 200 像素的 JPEG**，存入数据库。头像会显示在个人资料、在线列表和消息旁，重新登录后仍会加载。
- **翻译**：点击消息旁的地球图标，选择目标语言。支持中文、英文、日文和韩文，可手动指定原文语言。服务端使用 MyMemory 并缓存译文。首次跨语种翻译需要联网，单次原文上限为 500 个 UTF-8 字节。
- **语音输入**：在支持语音识别的浏览器中通过 localhost 或 HTTPS 访问并允许麦克风权限。识别语言为普通话，默认 30 秒后停止。可在「设置」调整时限、检测麦克风音量和查看诊断记录。自动发送为可选项。

## 数据库

H2 默认保存到运行 TomEE 的用户主目录下的 `~/jspchatroom-db/chatroom.mv.db`。首次运行会自动建表。已有数据库在启动时会自动补建头像表，账号、消息和翻译缓存可继续使用。

若旧部署使用其他数据库路径，先停止旧实例并备份，再给 TomEE 的 JVM 添加以下选项。请将示例路径改成实际绝对路径，末尾省略 `.mv.db`。同一数据库文件应由一个运行实例使用。

```text
-Dchatroom.jdbc.url=jdbc:h2:file:/绝对路径/chatroom
```

## 项目结构

- `src/main/java/edu/uestc/jspchatroom/web/`：Servlet 控制器和头像、翻译接口。
- `src/main/java/edu/uestc/jspchatroom/service/`：账户、聊天、翻译和头像处理。
- `src/main/java/edu/uestc/jspchatroom/dao/`：通过 JDBC 访问 H2。
- `src/main/webapp/WEB-INF/views/`：JSP 页面和消息、在线用户等视图片段。
- `src/main/webapp/assets/`：布局、主题和浏览器脚本。
- `src/main/resources/`：中英文界面文案和默认数据库配置。
