<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="title.login"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/login.css">
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
