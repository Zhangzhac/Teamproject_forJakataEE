# JSP 聊天室

本模块使用 Apache TomEE 10 Web Profile（Jakarta EE 10）与 JDK 17 或更新版本。当前版本已在 TomEE 10.3.0 和 JDK 21 下验证。

## 构建与部署

1. 安装 JDK 并配置 `JAVA_HOME`。从 [Apache TomEE 下载页](https://tomee.apache.org/download.html) 下载并解压 TomEE 10 的 Web Profile 发行包。
2. 在本模块目录运行 `.\mvnw.cmd clean package`（Windows）或 `./mvnw clean package`（Linux/macOS）。
3. 将 `target/jspchatroom.war` 复制到 TomEE 的 `webapps` 目录，运行 `bin\catalina.bat run`（Windows）或 `bin/catalina.sh run`（Linux/macOS）。
4. 打开 `http://localhost:8080/jspchatroom/`。若调整过 TomEE 的 HTTP 端口，请使用实际端口。

WAR 包含 H2 驱动。Servlet、JSP 和 JSTL 由 TomEE 提供。本机部署使用 `http://localhost:18080/jspchatroom/`。

## 代码分层

- `src/main/java/.../web` 包含 Servlet 控制器。原有的 `login.jsp`、`register.jsp`、`chat.jsp`、`logout.jsp`、`settings.jsp` URL 由控制器响应。
- `service` 处理账户、发消息、在线列表查询、翻译与缓存。`dao` 和 `model` 负责 JDBC 数据访问和数据对象。
- `src/main/webapp/WEB-INF/views` 包含 JSP 视图，只使用 EL 和 JSTL 渲染模型。`assets` 包含 CSS 与浏览器脚本。
- 浏览器每两秒通过聊天控制器读取消息和在线用户片段。发送消息和翻译各走独立请求，输入框与已有译文仍由浏览器保留。

## 使用已有数据库

数据库表、用户密码摘要格式和翻译缓存键保持兼容。默认使用 `~/jspchatroom-db/chatroom.mv.db`，首次启动自动建表。若旧部署使用其他路径，先停止旧服务器并备份 `.mv.db`，再在 TomEE 的 JVM 选项中设置 `-Dchatroom.jdbc.url=jdbc:h2:file:/绝对路径/chatroom`，路径末尾省略 `.mv.db`。同一数据库文件应由一个运行实例使用。

语音识别由浏览器提供，请使用支持该功能的浏览器，通过 localhost 或 HTTPS 访问并允许麦克风权限。MyMemory 翻译仍使用免注册接口，跨语言翻译需要联网。
