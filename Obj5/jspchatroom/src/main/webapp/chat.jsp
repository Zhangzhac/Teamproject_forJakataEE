<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, java.text.*" %>
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
    /* HTML 转义，防止 XSS */
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
        // PRG 模式：提交后重定向，避免刷新重复发消息
        response.sendRedirect(request.getContextPath() + "/chat.jsp");
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
    for (Object[] o : onlineSnapshot) {
        Date d = (Date) o[1];
        if (lastActive == null || d.after(lastActive)) lastActive = d;
    }
    String lastActiveStr = (lastActive == null) ? "-" : sdf.format(lastActive);
%>
<!doctype html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>聊天室（JSP 版）</title>
    <style>
        body { font-family: system-ui, -apple-system, Segoe UI, Roboto, "PingFang SC", "Microsoft YaHei", sans-serif; }
        ul { padding-left: 1.2em; }
        textarea { width: 420px; }
        .meta { color: #666; font-size: 12px; }
    </style>
</head>
<body>
<p>当前用户：<%= esc(currentUser) %>
    | 在线会话数：<%= onlineCount %>
    | 最近活跃：<%= lastActiveStr %>
</p>
<h3>消息列表</h3>
<% if (msgSnapshot.isEmpty()) { %>
<p class="meta">还没有消息，来说点什么吧~</p>
<% } else { %>
<ul>
    <%
        for (ChatMessage m : msgSnapshot) {
    %>
    <li>
        <span class="meta">[<%= sdf.format(m.time) %>]</span>
        <b><%= esc(m.user) %></b>：
        <%= esc(m.content) %>
    </li>
    <%
        }
    %>
</ul>
<% } %>
<form method="post" action="chat.jsp">
    <textarea name="msg" rows="3" maxlength="500" placeholder="说点什么..."></textarea><br/>
    <button type="submit">发送</button>
</form>
<h3>在线用户</h3>
<%
    // 同一用户多标签页（同 session）会合并显示
    Set<String> names = new LinkedHashSet<String>();
    Map<String, Date> nameLast = new LinkedHashMap<String, Date>();
    for (Object[] o : onlineSnapshot) {
        String name = (String) o[0];
        Date d = (Date) o[1];
        names.add(name);
        Date prev = nameLast.get(name);
        if (prev == null || d.after(prev)) nameLast.put(name, d);
    }
%>
<% if (names.isEmpty()) { %>
<p class="meta">暂无</p>
<% } else { %>
<ul>
    <%
        for (String name : names) {
    %>
    <li><%= esc(name) %>
        <span class="meta">（最近活跃 <%= sdf.format(nameLast.get(name)) %>）</span>
    </li>
    <%
        }
    %>
</ul>
<% } %>
<form method="post" action="logout.jsp">
    <button>退出登录</button>
</form>
</body>
</html>