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
    <title><fmt:message key="title.chat"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css?v=3">
    <script src="${pageContext.request.contextPath}/assets/js/theme.js?v=1"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/chat.css?v=6">
</head>
<body class="chat-page" data-context-path="${pageContext.request.contextPath}">
<div class="chat-shell">
    <aside class="sidebar">
        <div class="brand">
            <span class="brand-mark" aria-hidden="true"><span></span></span>
            <span><fmt:message key="title.chat"/></span>
        </div>

        <div class="presence">
            <h2 class="presence-heading">
                <span><span class="online-dot" aria-hidden="true"></span> <fmt:message key="title.onlineUsers"/></span>
                <span class="presence-count" id="onlineUserCount">${onlineUserCount}</span>
            </h2>
            <%@ include file="fragments/online-users.jspf" %>
        </div>

        <div class="sidebar-footer">
            <div class="account">
                <%@ include file="fragments/account-avatar.jspf" %>
                <div class="account-detail">
                    <strong><c:out value="${currentUser.username}"/></strong>
                    <time title="<fmt:message key="label.lastLogin"/>"><fmt:formatDate value="${currentUser.lastLoginAt}" pattern="yyyy-MM-dd HH:mm"/></time>
                </div>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/logout.jsp">
                <input type="hidden" name="lang" value="${pageLang}">
                <button class="button button-logout" type="submit"><fmt:message key="button.logout"/></button>
            </form>
        </div>
    </aside>

    <main class="conversation" aria-labelledby="conversationTitle">
        <header class="conversation-header">
            <div class="conversation-identity">
                <span class="room-mark" aria-hidden="true">#</span>
                <div>
                    <h1 id="conversationTitle"><fmt:message key="room.general"/></h1>
                    <p><fmt:message key="label.totalMessages"/> <span id="totalMessageCount">${totalMessages}</span></p>
                </div>
            </div>
            <div class="conversation-preferences">
                <label class="sr-only" for="chatTheme"><fmt:message key="theme.choose"/></label>
                <select class="theme-select" id="chatTheme" title="<fmt:message key="theme.choose"/>" disabled>
                    <option value="default"><fmt:message key="theme.default"/></option>
                    <option value="coder"><fmt:message key="theme.coder"/></option>
                    <option value="union"><fmt:message key="theme.union"/></option>
                </select>
                <%@ include file="fragments/language.jspf" %>
            </div>
        </header>

        <div id="messageFeed" class="message-feed" tabindex="0" aria-labelledby="conversationTitle">
            <%@ include file="fragments/messages.jspf" %>
        </div>

        <div class="composer-area">
            <fmt:message key="message.placeholder" var="msgPlaceholder"/>
            <form id="messageForm" class="composer" data-voice-scope="${pageContext.request.contextPath}:${currentUser.id}" method="post" action="${pageContext.request.contextPath}/chat.jsp">
                <input type="hidden" name="lang" value="${pageLang}">
                <label class="sr-only" for="messageInput"><c:out value="${msgPlaceholder}"/></label>
                <textarea id="messageInput" name="msg" rows="2" maxlength="500" aria-describedby="messageShortcuts" placeholder="<c:out value='${msgPlaceholder}'/>" required></textarea>
                <div class="composer-actions">
                    <div class="composer-tools">
                        <button class="button button-quiet" type="button" id="micBtn" aria-pressed="false" title="<fmt:message key="extension.label1"/>"><fmt:message key="extension.label3"/></button>
                        <a class="button button-quiet" href="${pageContext.request.contextPath}/settings.jsp?lang=${pageLang}" target="_blank" rel="noopener"><fmt:message key="extension.label5"/></a>
                        <label class="auto-send"><input type="checkbox" id="voiceAutoSend"> <fmt:message key="extension.label2"/></label>
                    </div>
                    <button class="button button-primary button-send" type="submit">
                        <span class="send-label">
                            <span class="send-ready"><fmt:message key="button.send"/></span>
                            <span class="send-pending"></span>
                        </span>
                        <span class="send-icon" aria-hidden="true">&uarr;</span>
                    </button>
                </div>
            </form>
            <p id="messageShortcuts" class="meta voice-tip"><fmt:message key="message.shortcuts"/></p>
            <p id="voiceTip" class="meta voice-tip" role="status" aria-live="polite"><fmt:message key="extension.label0"/></p>
        </div>
    </main>
</div>
<script src="${pageContext.request.contextPath}/assets/js/chat-i18n.js?v=2"></script>
<script src="${pageContext.request.contextPath}/assets/js/chat.js?v=5"></script>
<script src="${pageContext.request.contextPath}/assets/js/voice-input.js?v=5"></script>
</body>
</html>
