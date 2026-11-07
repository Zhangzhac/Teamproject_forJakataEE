<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.text.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%!
    /* 一条聊天消息 */
    public static class ChatMessage {
        public final String user;
        public final Date time;
        public final String content;
        public ChatMessage(String user, Date time, String content) {
            this.user = user;
            this.time = time;
            this.content = content;
        }
    }
    /* HTML 转义，防止用户名等数据破坏页面 */
    public static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }
%>
<%
    request.setCharacterEncoding("UTF-8");
    // ============ 读取 maxMessageCount（context-param） ============
    int maxMessageCount = 100; // 默认值
    String maxParam = application.getInitParameter("maxMessageCount");
    if (maxParam != null) {
        try {
            maxMessageCount = Integer.parseInt(maxParam.trim());
        } catch (NumberFormatException ignore) { /* 用默认值 */ }
    }
    if (maxMessageCount <= 0) maxMessageCount = 100;
    // ============ 读取（或初始化）全局结构 ============
    List<ChatMessage> messages;              // 全局消息列表
    Map<String, Object[]> onlineSessions;    // sessionId -> {username, lastActiveDate}
    synchronized (application) {
        Object m = application.getAttribute("chatMessages");
        if (m instanceof List) {
            messages = (List<ChatMessage>) m;
        } else {
            messages = Collections.synchronizedList(new ArrayList<ChatMessage>());
            application.setAttribute("chatMessages", messages);
        }
        Object u = application.getAttribute("onlineSessions");
        if (u instanceof Map) {
            onlineSessions = (Map<String, Object[]>) u;
        } else {
            onlineSessions = Collections.synchronizedMap(
                    new LinkedHashMap<String, Object[]>());
            application.setAttribute("onlineSessions", onlineSessions);
        }
    }
    // ============ 登录状态检查 ============
    String currentUser = (String) session.getAttribute("username");
    if (currentUser == null || currentUser.trim().isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return; // 不再输出本页
    }
    // 刷新当前会话的活跃时间
    String sessionId = session.getId();
    onlineSessions.put(sessionId, new Object[]{ currentUser, new Date() });
    // ============ 处理发消息（POST） ============
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String rawMsg = request.getParameter("msg");
        if (rawMsg != null) {
            rawMsg = rawMsg.trim();
            if (rawMsg.length() > 500) {
                rawMsg = rawMsg.substring(0, 500); // 截断超长消息
            }
            if (!rawMsg.isEmpty()) {
                synchronized (messages) {
                    messages.add(new ChatMessage(currentUser, new Date(), rawMsg));
                    // 只保留最近 maxMessageCount 条
                    while (messages.size() > maxMessageCount) {
                        messages.remove(0);
                    }
                }
            }
        }
        // PRG 模式：提交后重定向，避免刷新重复发消息。
        // 带上当前语言，防止重定向后回退到浏览器默认语言。
        String lang = request.getParameter("lang");
        String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
        response.sendRedirect(request.getContextPath() + "/chat.jsp" + langQuery);
        return;
    }
    // ============ 准备展示数据（快照，避免边遍历边修改） ============
    List<ChatMessage> msgSnapshot;
    synchronized (messages) {
        msgSnapshot = new ArrayList<ChatMessage>(messages);
    }
    List<Object[]> onlineSnapshot;
    synchronized (onlineSessions) {
        onlineSnapshot = new ArrayList<Object[]>(onlineSessions.values());
    }
    int onlineCount = onlineSnapshot.size();
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    Date lastActive = null;
    Set<String> names = new LinkedHashSet<String>();
    for (Object[] o : onlineSnapshot) {
        names.add((String) o[0]);
        Date d = (Date) o[1];
        if (lastActive == null || d.after(lastActive)) lastActive = d;
    }
    String lastActiveStr = (lastActive == null) ? "-" : sdf.format(lastActive);

    // ============ 转换为 JSTL/EL 使用的 request 属性 ============
    // 消息先拼成 HTML 片段；按实验要求消息正文不转义，因此页面用
    // <c:out escapeXml="false"> 输出。生产环境应对消息做白名单过滤。
    List<String> msgs = new ArrayList<String>();
    for (ChatMessage m : msgSnapshot) {
        msgs.add("<span class=\"meta\">[" + sdf.format(m.time) + "]</span> <b>"
                + esc(m.user) + "</b>：" + m.content);
    }
    List<String> users = new ArrayList<String>(names);

    request.setAttribute("currentUser", currentUser);
    request.setAttribute("onlineCount", onlineCount);
    request.setAttribute("lastActiveStr", lastActiveStr);
    request.setAttribute("msgs", msgs);
    request.setAttribute("users", users);
%>
<c:set var="pageLang" value="${param.lang == 'en' ? 'en' : (param.lang == 'zh' ? 'zh' : pageContext.request.locale.language)}" />
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<!doctype html>
<html lang="${pageLang}">
<head>
    <meta charset="UTF-8">
    <title><fmt:message key="title.chat"/></title>
    <style>
        body { font-family: system-ui, -apple-system, Segoe UI, Roboto, "PingFang SC", "Microsoft YaHei", sans-serif; }
        ul { padding-left: 1.2em; }
        textarea { width: 420px; }
        .meta { color: #666; font-size: 12px; }
        .lang { font-size: 12px; }
    </style>
</head>
<body>
<p><fmt:message key="label.currentUser"/>: ${currentUser}
    | <fmt:message key="label.onlineSessions"/>: ${onlineCount}
    | <fmt:message key="label.lastActive"/>: ${lastActiveStr}
</p>
<p class="lang">
    <fmt:message key="language.switch"/>:
    <a href="?lang=zh"><fmt:message key="language.chinese"/></a> |
    <a href="?lang=en"><fmt:message key="language.english"/></a>
</p>
<h3><fmt:message key="title.messages"/></h3>
<c:choose>
    <c:when test="${empty msgs}">
        <p class="meta"><fmt:message key="message.noMessages"/></p>
    </c:when>
    <c:otherwise>
        <ul>
            <c:forEach var="m" items="${msgs}">
                <li><c:out value="${m}" escapeXml="false"/></li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>
<fmt:message key="message.placeholder" var="msgPlaceholder" />
<form method="post" action="${pageContext.request.contextPath}/chat.jsp">
    <%-- 发送消息时一起提交当前语言，重定向后仍保持同一种语言 --%>
    <input type="hidden" name="lang" value="${pageLang}">
    <textarea name="msg" rows="3" maxlength="500" placeholder="${msgPlaceholder}"></textarea><br/>
    <button type="submit"><fmt:message key="button.send"/></button>
</form>
<h3><fmt:message key="title.onlineUsers"/></h3>
<c:choose>
    <c:when test="${empty users}">
        <p class="meta"><fmt:message key="message.noUsers"/></p>
    </c:when>
    <c:otherwise>
        <ul>
            <c:forEach var="u" items="${users}">
                <li><c:out value="${u}"/></li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>
<form method="post" action="${pageContext.request.contextPath}/logout.jsp">
    <button><fmt:message key="button.logout"/></button>
</form>
</body>
</html>
