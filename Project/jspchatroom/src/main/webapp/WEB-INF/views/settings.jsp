<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><fmt:message key="extension.label13"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=3">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js?v=1"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/settings.css?v=4">
</head>
<body data-voice-scope="${pageContext.request.contextPath}:${currentUser.id}">
    <main class="settings-layout">
    <header class="settings-header">
        <div class="brand"><span class="brand-mark" aria-hidden="true"><span></span></span><span><fmt:message key="title.chat"/></span></div>
        <%@ include file="fragments/language.jspf" %>
    </header>
    <h1><fmt:message key="extension.label20"/></h1>
    <p class="meta back-note"><a href="${pageContext.request.contextPath}/chat.jsp?lang=${pageLang}"><fmt:message key="extension.label18"/></a><fmt:message key="extension.label10"/></p>
    <%@ include file="fragments/avatar-settings.jspf" %>
    <section class="settings-card">
        <h2><fmt:message key="extension.label16"/></h2>
        <label for="voiceDuration"><fmt:message key="extension.label12"/></label>
        <select id="voiceDuration">
            <option value="15"><fmt:message key="extension.label21"/></option>
            <option value="30" selected><fmt:message key="extension.label14"/></option>
            <option value="60"><fmt:message key="extension.label22"/></option>
            <option value="120"><fmt:message key="extension.label19"/></option>
        </select>
        <p class="meta"><fmt:message key="extension.label6"/></p>
        <p class="meta"><fmt:message key="extension.label11"/></p>
        <p id="settingsStatus" role="status" aria-live="polite"></p>
    </section>
    <section class="settings-card">
        <h2><fmt:message key="extension.label15"/></h2>
        <p><fmt:message key="extension.label9"/></p>
        <p id="micTestAnchor" class="meta"><fmt:message key="extension.label8"/></p>
    </section>
    <section class="settings-card">
        <h2><fmt:message key="extension.label17"/></h2>
        <p class="meta"><fmt:message key="extension.label7"/></p>
        <pre id="voiceLog"></pre>
        <button type="button" id="clearVoiceLog"><fmt:message key="extension.label23"/></button>
    </section>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/chat-i18n.js?v=2"></script>
    <script src="${pageContext.request.contextPath}/assets/js/voice-settings.js?v=3"></script>
    <script src="${pageContext.request.contextPath}/assets/js/avatar-settings.js?v=1"></script>
</body>
</html>
