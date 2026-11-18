<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="title.register"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/register.css">
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
