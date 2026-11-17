<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ page import="edu.hyw.jspchatroom.model.User" %>
<%@ page import="edu.hyw.jspchatroom.dao.UserDao" %>
<%@ page import="edu.hyw.jspchatroom.util.PasswordUtil" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    登录页（实验 07 步骤 3）
    ---------------------------------------------------------------
    与第 06 次实验不同：这里不再“输入用户名就放行”，而是拿着
    用户名 + 密码去 users 表里查，密码用加盐 SHA-256 摘要比较，
    校验通过才创建会话并跳转到 chat.jsp。

    处理流程：
      GET  -> 显示登录表单
      POST -> ① 非空校验 ② 查库 ③ 比对密码摘要 ④ 更新最近登录时间
              ⑤ 写 session ⑥ 重定向到 chat.jsp（PRG，避免刷新重复提交）
--%>
<%
    request.setCharacterEncoding("UTF-8");   // 防止中文用户名乱码

    String username = request.getParameter("username");
    username = (username == null) ? "" : username.trim();
    String password = request.getParameter("password");

    UserDao userDao = new UserDao();
    // DBInitListener 初始化失败时给它一个明确的提示，而不是直接 500
    boolean dbReady = Boolean.TRUE.equals(application.getAttribute("dbReady"));
    int registeredUsers = 0;

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        if (username.isEmpty()) {
            request.setAttribute("error", "error.username.empty");
        } else if (password == null || password.isEmpty()) {
            request.setAttribute("error", "error.password.empty");
        } else if (!dbReady) {
            request.setAttribute("error", "error.db.notReady");
        } else {
            // ① 按用户名从数据库读取用户（PreparedStatement，防 SQL 注入）
            User found = userDao.findByUsername(username);
            // ② 比对密码：数据库中存的是“盐$摘要”，不能直接比较明文
            if (found == null || !PasswordUtil.matches(password, found.getPassword())) {
                // 用户名不存在与密码错误使用同一提示，避免暴露账号是否存在
                request.setAttribute("error", "error.login.failed");
            } else {
                // ③ 登录成功：更新“最近登录时间”并把在线状态置为 TRUE
                userDao.markLogin(found.getId());
                found.setOnline(true);
                found.setLastLoginAt(new java.util.Date());

                // ④ 本页 session="false"（没有隐式 session 对象），必须显式创建
                HttpSession chatSession = request.getSession(true);
                chatSession.setAttribute("currentUser", found);
                chatSession.setAttribute("username", found.getUsername());
                chatSession.setMaxInactiveInterval(30 * 60); // 30 分钟不活动则失效

                // ⑤ 登录成功后继续使用当前语言，避免跳到聊天页后变回浏览器默认语言
                String lang = request.getParameter("lang");
                String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
                response.sendRedirect(request.getContextPath() + "/chat.jsp" + langQuery);
                return;   // 重定向后必须 return，不再输出下面页面
            }
        }
    }

    if (dbReady) {
        registeredUsers = userDao.countUsers();
    }
    // 交给 EL 输出
    request.setAttribute("registeredUsers", registeredUsers);
%>
<c:set var="pageLang" value="${param.lang == 'en' ? 'en' : (param.lang == 'zh' ? 'zh' : pageContext.request.locale.language)}" />
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="title.login"/></title>
    <style>
        body { font-family: system-ui, -apple-system, "Segoe UI", "Microsoft YaHei", sans-serif; }
        .error { color: #c00; }
        .success { color: #080; }
        .lang, .hint { font-size: 12px; color: #666; }
        label { display: inline-block; width: 90px; }
        .row { margin: 6px 0; }
    </style>
</head>
<body>
<h2><fmt:message key="title.login"/></h2>
<p class="lang">
    <fmt:message key="language.switch"/>:
    <a href="?lang=zh"><fmt:message key="language.chinese"/></a> |
    <a href="?lang=en"><fmt:message key="language.english"/></a>
</p>

<%-- 可选改进（实验文档步骤 2 提示）：用 JSTL 的 c:if 显示 ${error} --%>
<c:if test="${not empty error}">
    <p class="error"><fmt:message key="${error}"/></p>
</c:if>
<%-- 从注册页跳回来时显示“注册成功” --%>
<c:if test="${param.registered == '1'}">
    <p class="success"><fmt:message key="message.registerSuccess"/></p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/login.jsp">
    <%-- 提交时把当前语言一起带回，校验失败后仍显示同一种语言 --%>
    <input type="hidden" name="lang" value="${pageLang}">
    <div class="row">
        <label for="username"><fmt:message key="label.username"/>:</label>
        <input id="username" name="username" maxlength="20" value="<c:out value='${param.username}'/>" autofocus>
    </div>
    <div class="row">
        <label for="password"><fmt:message key="label.password"/>:</label>
        <input id="password" name="password" type="password" maxlength="32">
    </div>
    <button type="submit"><fmt:message key="button.enter"/></button>
</form>

<p><a href="${pageContext.request.contextPath}/register.jsp?lang=${pageLang}"><fmt:message key="link.toRegister"/></a></p>

<p class="hint">
    <fmt:message key="label.registeredUsers"/>: <strong>${registeredUsers}</strong>
    <c:if test="${not empty applicationScope.dbFile}">
        <br/><fmt:message key="label.dbLocation"/>: <code><c:out value="${applicationScope.dbFile}"/></code>
    </c:if>
</p>
</body>
</html>
