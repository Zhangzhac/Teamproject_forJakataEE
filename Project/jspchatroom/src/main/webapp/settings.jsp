<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.hyw.jspchatroom.model.User" %>
<%
    response.setHeader("Cache-Control", "no-store");
    if (!(session.getAttribute("currentUser") instanceof User user) || user.getId() <= 0) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?lang=" + ("en".equals(request.getParameter("lang")) ? "en" : "zh"));
        return;
    }
%>
<c:set var="pageLang" value="${param.lang == 'en' ? 'en' : (param.lang == 'zh' ? 'zh' : (pageContext.request.locale.language == 'en' ? 'en' : 'zh'))}" />
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><fmt:message key="extension.label13"/></title>
    <style>
        body { font-family: system-ui, "Microsoft YaHei", sans-serif; max-width: 680px; margin: 40px auto; padding: 0 20px; line-height: 1.7; color: #263238; }
        section { border: 1px solid #ddd; border-radius: 10px; padding: 20px; margin: 20px 0; }
        button, select { padding: 8px 12px; font: inherit; }
        .note { color: #666; font-size: 14px; }
    </style>
</head>
<body data-voice-scope="${pageContext.request.contextPath}:<%= user.getId() %>">
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
    <script src="${pageContext.request.contextPath}/chat-i18n.js?v=1"></script>
    <script src="${pageContext.request.contextPath}/voice-settings.js?v=3"></script>
</body>
</html>
