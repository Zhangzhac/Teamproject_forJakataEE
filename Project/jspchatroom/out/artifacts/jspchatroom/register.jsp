<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ page import="edu.hyw.jspchatroom.model.User" %>
<%@ page import="edu.hyw.jspchatroom.dao.UserDao" %>
<%@ page import="edu.hyw.jspchatroom.util.PasswordUtil" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    注册页（实验 07 步骤 3）
    ---------------------------------------------------------------
    GET  -> 显示注册表单（用户名 + 密码 + 确认密码）
    POST -> 校验通过后把用户写入 users 表，然后重定向回 login.jsp
            提示“注册成功，请登录”。

    安全做法：数据库中不保存明文密码，
    PasswordUtil.hash() 会生成随机盐并用 SHA-256 计算摘要，
    users.password 保存的是 “盐$摘要”。
--%>
<%
    request.setCharacterEncoding("UTF-8");

    String username = request.getParameter("username");
    username = (username == null) ? "" : username.trim();
    String password = request.getParameter("password");
    String confirm = request.getParameter("confirm");

    UserDao userDao = new UserDao();
    boolean dbReady = Boolean.TRUE.equals(application.getAttribute("dbReady"));

    if ("POST".equalsIgnoreCase(request.getMethod())) {
        if (username.isEmpty()) {
            request.setAttribute("error", "error.username.empty");
        } else if (username.length() > 20) {
            request.setAttribute("error", "error.username.tooLong");
        } else if (!username.matches("[\\p{L}\\p{N}_]{2,20}")) {
            // 只允许字母（含中文）、数字、下划线，长度 2~20
            request.setAttribute("error", "error.username.invalid");
        } else if (password == null || password.isEmpty()) {
            request.setAttribute("error", "error.password.empty");
        } else if (password.length() < 6) {
            request.setAttribute("error", "error.password.tooShort");
        } else if (password.length() > 32) {
            request.setAttribute("error", "error.password.tooLong");
        } else if (confirm == null || !confirm.equals(password)) {
            request.setAttribute("error", "error.password.mismatch");
        } else if (!dbReady) {
            request.setAttribute("error", "error.db.notReady");
        } else if (userDao.existsByUsername(username)) {
            request.setAttribute("error", "error.user.exists");
        } else {
            // 写入数据库：密码先加盐摘要，再入库
            User created = userDao.create(username, PasswordUtil.hash(password));
            System.out.println("[register] 新用户注册成功: " + created.getUsername() + " (id=" + created.getId() + ")");
            String lang = request.getParameter("lang");
            String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
            // PRG：注册成功后重定向到登录页，避免刷新导致重复注册
            response.sendRedirect(request.getContextPath()
                    + "/login.jsp?registered=1" + (langQuery.isEmpty() ? "" : "&" + langQuery.substring(1)));
            return;
        }
    }
%>
<c:set var="pageLang" value="${param.lang == 'en' ? 'en' : (param.lang == 'zh' ? 'zh' : pageContext.request.locale.language)}" />
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="title.register"/></title>
    <style>
        body { font-family: system-ui, -apple-system, "Segoe UI", "Microsoft YaHei", sans-serif; }
        .error { color: #c00; }
        .lang, .hint { font-size: 12px; color: #666; }
        label { display: inline-block; width: 90px; }
        .row { margin: 6px 0; }
    </style>
</head>
<body>
<h2><fmt:message key="title.register"/></h2>
<p class="lang">
    <fmt:message key="language.switch"/>:
    <a href="?lang=zh"><fmt:message key="language.chinese"/></a> |
    <a href="?lang=en"><fmt:message key="language.english"/></a>
</p>

<c:if test="${not empty error}">
    <p class="error"><fmt:message key="${error}"/></p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/register.jsp">
    <input type="hidden" name="lang" value="${pageLang}">
    <div class="row">
        <label for="username"><fmt:message key="label.username"/>:</label>
        <input id="username" name="username" maxlength="20" value="<c:out value='${param.username}'/>" autofocus>
    </div>
    <div class="row">
        <label for="password"><fmt:message key="label.password"/>:</label>
        <input id="password" name="password" type="password" maxlength="32">
    </div>
    <div class="row">
        <label for="confirm"><fmt:message key="label.password.confirm"/>:</label>
        <input id="confirm" name="confirm" type="password" maxlength="32">
    </div>
    <button type="submit"><fmt:message key="button.register"/></button>
</form>

<p class="hint"><fmt:message key="message.passwordRule"/></p>
<p><a href="${pageContext.request.contextPath}/login.jsp?lang=${pageLang}"><fmt:message key="link.toLogin"/></a></p>
</body>
</html>
