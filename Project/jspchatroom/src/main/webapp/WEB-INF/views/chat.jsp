<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="title.chat"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/chat.css?v=2">
</head>
<body data-context-path="${pageContext.request.contextPath}">
<p>
    <fmt:message key="label.currentUser"/>:
    <strong class="me"><c:out value="${currentUser.username}"/></strong>
    | <fmt:message key="label.lastLogin"/>:
    <fmt:formatDate value="${currentUser.lastLoginAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
    | <fmt:message key="label.onlineUsers"/>: <span id="onlineUserCount">${onlineUserCount}</span>
    | <fmt:message key="label.totalMessages"/>: <span id="totalMessageCount">${totalMessages}</span>
</p>
<p class="lang">
    <fmt:message key="language.switch"/>:
    <a href="?lang=zh"><fmt:message key="language.chinese"/></a> |
    <a href="?lang=en"><fmt:message key="language.english"/></a>
</p>

<div class="panel">
    <div class="messages">
        <h3><fmt:message key="title.messages"/></h3>
        <%@ include file="fragments/messages.jspf" %>

        <fmt:message key="message.placeholder" var="msgPlaceholder"/>
        <form id="messageForm" data-voice-scope="${pageContext.request.contextPath}:${currentUser.id}" method="post" action="${pageContext.request.contextPath}/chat.jsp">
            <%-- 发送消息时一起提交当前语言，重定向后仍保持同一种语言 --%>
            <input type="hidden" name="lang" value="${pageLang}">
            <textarea name="msg" rows="3" maxlength="500" placeholder="${msgPlaceholder}" required></textarea><br/>
            <button type="button" id="micBtn" aria-pressed="false" title="<fmt:message key="extension.label1"/>"><fmt:message key="extension.label3"/></button>
            <a href="${pageContext.request.contextPath}/settings.jsp?lang=${pageLang}" target="_blank" rel="noopener"><fmt:message key="extension.label5"/></a>
            <label><input type="checkbox" id="voiceAutoSend"> <fmt:message key="extension.label2"/></label>
            <button type="submit"><fmt:message key="button.send"/></button>
            <p id="voiceTip" class="meta" role="status" aria-live="polite"><fmt:message key="extension.label0"/></p>
        </form>
    </div>

    <div class="sidebar">
        <h3><fmt:message key="title.onlineUsers"/></h3>
        <%@ include file="fragments/online-users.jspf" %>

        <form method="post" action="${pageContext.request.contextPath}/logout.jsp">
            <input type="hidden" name="lang" value="${pageLang}">
            <button type="submit"><fmt:message key="button.logout"/></button>
        </form>
    </div>
</div>

<%-- ============ 消息翻译（实验扩展） ============ --%>
<%-- 点消息旁的 🌐 → 展开语言按钮 → 请求 /api/translate → 把返回的 HTML 片段
     插到这条消息下面的 .tr-slot 里。再点一次 🌐 收起。 --%>
<script src="${pageContext.request.contextPath}/assets/js/chat-i18n.js?v=1"></script>
<script src="${pageContext.request.contextPath}/assets/js/chat.js?v=1"></script>
<script src="${pageContext.request.contextPath}/assets/js/voice-input.js?v=5"></script>
</body>
</html>
