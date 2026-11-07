<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    // 处理 POST（提交登录）：验证用户名、创建会话并重定向
    request.setCharacterEncoding("UTF-8");   // 防止中文用户名乱码
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String username = request.getParameter("username");
        if (username != null) {
            username = username.trim();
        }
        if (username == null || username.isEmpty()) {
            request.setAttribute("error", "error.username.empty");
        } else if (username.length() > 20) {
            request.setAttribute("error", "error.username.tooLong");
        } else {
            // 注意：本页 session="false"，没有隐式 session 对象，
            // 必须通过 request.getSession(true) 显式创建
            HttpSession chatSession = request.getSession(true);
            chatSession.setAttribute("username", username);
            chatSession.setMaxInactiveInterval(30 * 60); // 30 分钟不活动则失效
            // 登录成功后继续使用当前语言，避免跳到聊天页后变回浏览器默认语言
            String lang = request.getParameter("lang");
            String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
            response.sendRedirect(request.getContextPath() + "/chat.jsp" + langQuery);
            return;   // 重定向后必须 return，不再输出下面页面
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
    <title><fmt:message key="title.login"/></title>
    <style>
        body { font-family: system-ui, -apple-system, "Segoe UI", "Microsoft YaHei", sans-serif; }
        .error { color: #c00; }
        .lang { font-size: 12px; }
    </style>
</head>
<body>
<h2><fmt:message key="title.login"/></h2>
<p class="lang">
    <fmt:message key="language.switch"/>:
    <a href="?lang=zh"><fmt:message key="language.chinese"/></a> |
    <a href="?lang=en"><fmt:message key="language.english"/></a>
</p>
<c:if test="${not empty error}">
    <p class="error"><fmt:message key="${error}"/></p>
</c:if>
<form method="post" action="${pageContext.request.contextPath}/login.jsp">
    <%-- 提交时把当前语言一起带回，校验失败后仍显示同一种语言 --%>
    <input type="hidden" name="lang" value="${pageLang}">
    <label for="username"><fmt:message key="label.username"/>:</label>
    <input id="username" name="username" maxlength="20" autofocus>
    <button type="submit"><fmt:message key="button.enter"/></button>
</form>
</body>
</html>
