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
    <title><fmt:message key="extension.label13"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/settings.css">
</head>
<body data-voice-scope="${pageContext.request.contextPath}:${currentUser.id}">
    <h1><fmt:message key="extension.label20"/></h1>
    <p><a href="${pageContext.request.contextPath}/chat.jsp?lang=${pageLang}"><fmt:message key="extension.label18"/></a><fmt:message key="extension.label10"/></p>
    <section>
        <h2><fmt:message key="extension.label16"/></h2>
        <label for="voiceDuration"><fmt:message key="extension.label12"/></label>
        <select id="voiceDuration">
            <option value="15"><fmt:message key="extension.label21"/></option>
            <option value="30" selected><fmt:message key="extension.label14"/></option>
            <option value="60"><fmt:message key="extension.label22"/></option>
            <option value="120"><fmt:message key="extension.label19"/></option>
        </select>
        <p class="note"><fmt:message key="extension.label6"/></p>
        <p class="note"><fmt:message key="extension.label11"/></p>
        <p id="settingsStatus" role="status" aria-live="polite"></p>
    </section>
    <section>
        <h2><fmt:message key="extension.label15"/></h2>
        <p><fmt:message key="extension.label9"/></p>
        <p id="micTestAnchor" class="note"><fmt:message key="extension.label8"/></p>
    </section>
    <section>
        <h2><fmt:message key="extension.label17"/></h2>
        <p class="note"><fmt:message key="extension.label7"/></p>
        <pre id="voiceLog" style="white-space: pre-wrap; overflow-wrap: anywhere;"></pre>
        <button type="button" id="clearVoiceLog"><fmt:message key="extension.label23"/></button>
    </section>
    <script src="${pageContext.request.contextPath}/assets/js/chat-i18n.js?v=1"></script>
    <script src="${pageContext.request.contextPath}/assets/js/voice-settings.js?v=3"></script>
</body>
</html>
