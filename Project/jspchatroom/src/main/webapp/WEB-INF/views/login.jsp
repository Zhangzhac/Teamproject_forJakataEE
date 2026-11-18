<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><fmt:message key="title.login"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=2">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js?v=1"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css?v=2">
</head>
<body class="auth-page">
<main class="auth-shell">
    <div class="auth-brand">
        <div class="brand"><span class="brand-mark" aria-hidden="true"><span></span></span><span><fmt:message key="title.chat"/></span></div>
        <div class="auth-art" aria-hidden="true">
            <span class="art-bubble"><i></i><i></i></span>
            <span class="art-bubble"><i></i><i></i></span>
            <span class="art-bubble"><i></i><i></i></span>
        </div>
    </div>
    <div class="auth-panel">
        <%@ include file="fragments/language.jspf" %>
        <h1><fmt:message key="title.login"/></h1>

<c:if test="${not empty error}">
    <p class="auth-feedback error" role="alert"><fmt:message key="${error}"/></p>
</c:if>
<c:if test="${param.registered == '1'}">
    <p class="auth-feedback success" role="status"><fmt:message key="message.registerSuccess"/></p>
</c:if>

<form class="auth-form" method="post" action="${pageContext.request.contextPath}/login.jsp">
        <input type="hidden" name="lang" value="${pageLang}">
    <div class="field">
        <label for="username"><fmt:message key="label.username"/>:</label>
        <input id="username" name="username" autocomplete="username" maxlength="20" value="<c:out value='${param.username}'/>" autofocus>
    </div>
    <div class="field">
        <label for="password"><fmt:message key="label.password"/>:</label>
        <input id="password" name="password" type="password" autocomplete="current-password" maxlength="32">
    </div>
    <button class="button button-primary" type="submit"><fmt:message key="button.enter"/></button>
</form>

<p class="auth-link"><a href="${pageContext.request.contextPath}/register.jsp?lang=${pageLang}"><fmt:message key="link.toRegister"/></a></p>

<div class="meta auth-meta">
    <fmt:message key="label.registeredUsers"/>: <strong>${registeredUsers}</strong>
    <c:if test="${not empty applicationScope.dbFile}">
        <details>
            <summary><fmt:message key="label.dbLocation"/></summary>
            <code><c:out value="${applicationScope.dbFile}"/></code>
        </details>
    </c:if>
</div>
    </div>
</main>
</body>
</html>
