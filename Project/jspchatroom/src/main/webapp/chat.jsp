<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.hyw.jspchatroom.model.User" %>
<%@ page import="edu.hyw.jspchatroom.model.Message" %>
<%@ page import="edu.hyw.jspchatroom.dao.MessageDao" %>
<%@ page import="edu.hyw.jspchatroom.dao.UserDao" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
    聊天室页面（实验 07 步骤 4）
    ---------------------------------------------------------------
    · 页面加载（GET）：用 MessageDao.findRecent(n) 从 messages 表读取
      最近 n 条记录（n 取自 web.xml 的 maxMessageCount），
      再用 JSTL <c:forEach> 遍历显示 —— 数据全部来自数据库。
    · 发送消息（POST）：把 用户编号 + 内容 + 时间 插入 messages 表，
      然后重定向回本页（PRG 模式），刷新不会重复发送。
    · 因为消息保存在 H2 文件数据库里，所以 Tomcat 重启后历史消息仍在；
      新注册的用户登录后同样能看到这些历史消息。
--%>
<%
    request.setCharacterEncoding("UTF-8");

    // ============ 1. 登录状态（AuthFilter 已拦截，这里做二次保险） ============
    Object current = session.getAttribute("currentUser");
    if (!(current instanceof User currentUser)) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    // ============ 2. 读取 maxMessageCount（web.xml 的 context-param） ============
    int maxMessageCount = MessageDao.DEFAULT_LIMIT;
    String maxParam = application.getInitParameter("maxMessageCount");
    if (maxParam != null) {
        try {
            maxMessageCount = Integer.parseInt(maxParam.trim());
        } catch (NumberFormatException ignore) {
            // 配置错误就用默认值
        }
    }
    if (maxMessageCount <= 0) {
        maxMessageCount = MessageDao.DEFAULT_LIMIT;
    }

    MessageDao messageDao = new MessageDao();

    // ============ 3. 处理发消息（POST） -> 写入数据库 ============
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        // EncodingFilter 已在 POST 时做过敏感词替换，优先使用过滤后的内容
        String msg = (String) request.getAttribute("filteredMsg");
        if (msg == null) {
            msg = request.getParameter("msg");
        }
        if (msg != null) {
            msg = msg.trim();
            if (msg.length() > 500) {
                msg = msg.substring(0, 500);   // 截断超长消息（与表字段长度一致）
            }
            if (!msg.isEmpty()) {
                // 核心：INSERT INTO messages (user_id, content, created_at) VALUES (?, ?, ?)
                int newId = messageDao.insert(currentUser.getId(), msg);
                System.out.println("[chat] 消息已保存 id=" + newId + ", user="
                        + currentUser.getUsername() + ", content=" + msg);
            }
        }
        // PRG：提交后重定向，避免刷新重复发消息；带上语言参数保持界面语言
        String lang = request.getParameter("lang");
        String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
        response.sendRedirect(request.getContextPath() + "/chat.jsp" + langQuery);
        return;
    }

    // ============ 4. 准备展示数据：全部来自数据库 ============
    List<Message> messages = messageDao.findRecent(maxMessageCount);   // 最近 N 条，按时间正序
    List<User> onlineUsers = new UserDao().findOnlineUsers();          // online = TRUE 的用户
    int totalMessages = messageDao.countMessages();

    request.setAttribute("messages", messages);
    request.setAttribute("onlineUsers", onlineUsers);
    request.setAttribute("onlineUserCount", onlineUsers.size());
    request.setAttribute("totalMessages", totalMessages);
    request.setAttribute("maxMessageCount", maxMessageCount);
    request.setAttribute("currentUser", currentUser);
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
        body { font-family: system-ui, -apple-system, "Segoe UI", "Microsoft YaHei", sans-serif; }
        .meta { color: #666; font-size: 12px; }
        .lang { font-size: 12px; }
        .panel { display: flex; gap: 40px; align-items: flex-start; }
        .messages { flex: 1 1 auto; }
        .sidebar { flex: 0 0 220px; }
        ul.msg { padding-left: 1.2em; }
        ul.msg li { margin: 3px 0; }
        textarea { width: 460px; }
        .me { color: #06c; }
        /* ---- 消息翻译（实验扩展） ---- */
        .tr-btn { border: none; background: none; cursor: pointer; font-size: 13px; padding: 0 4px; opacity: .45; }
        .tr-btn:hover { opacity: 1; }
        .tr-slot { margin: 3px 0 5px 2em; }
        .tr-slot button { margin-right: 6px; font-size: 12px; cursor: pointer; }
        .translation { color: #0a7d32; font-size: 13px; border-left: 3px solid #0a7d32; padding-left: 8px; }
        .translation .tr-lang { color: #888; font-size: 12px; }
        .translation.err { color: #b00; border-left-color: #b00; }
    </style>
</head>
<body>
<p>
    <fmt:message key="label.currentUser"/>:
    <strong class="me"><c:out value="${currentUser.username}"/></strong>
    | <fmt:message key="label.lastLogin"/>:
    <fmt:formatDate value="${currentUser.lastLoginAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
    | <fmt:message key="label.onlineUsers"/>: ${onlineUserCount}
    | <fmt:message key="label.totalMessages"/>: ${totalMessages}
</p>
<p class="lang">
    <fmt:message key="language.switch"/>:
    <a href="?lang=zh"><fmt:message key="language.chinese"/></a> |
    <a href="?lang=en"><fmt:message key="language.english"/></a>
</p>

<div class="panel">
    <div class="messages">
        <h3><fmt:message key="title.messages"/></h3>
        <%-- 实验要求：用 JSTL <c:forEach> 遍历数据库数据实现动态显示 --%>
        <c:choose>
            <c:when test="${empty messages}">
                <p class="meta"><fmt:message key="message.noMessages"/></p>
            </c:when>
            <c:otherwise>
                <ul class="msg" id="msgList">
                    <c:forEach var="m" items="${messages}">
                        <li data-id="${m.id}">
                            <span class="meta">[<fmt:formatDate value="${m.createdAt}" pattern="MM-dd HH:mm"/>]</span>
                            <b><c:out value="${m.username}"/></b>:
                            <%-- c:out 默认转义 HTML，防止消息内容中的标签破坏页面 --%>
                            <c:out value="${m.content}"/>
                            <%-- 翻译入口：点 🌐 选目标语言，译文插到本行下面的 .tr-slot 里 --%>
                            <button type="button" class="tr-btn" data-id="${m.id}" title="翻译这条消息">🌐</button>
                            <div class="tr-slot" data-id="${m.id}"></div>
                        </li>
                    </c:forEach>
                </ul>
                <p class="meta">
                    <fmt:message key="message.showingRecent">
                        <fmt:param value="${messages.size()}"/>
                        <fmt:param value="${totalMessages}"/>
                        <fmt:param value="${maxMessageCount}"/>
                    </fmt:message>
                </p>
            </c:otherwise>
        </c:choose>

        <fmt:message key="message.placeholder" var="msgPlaceholder"/>
        <form method="post" action="${pageContext.request.contextPath}/chat.jsp">
            <%-- 发送消息时一起提交当前语言，重定向后仍保持同一种语言 --%>
            <input type="hidden" name="lang" value="${pageLang}">
            <textarea name="msg" rows="3" maxlength="500" placeholder="${msgPlaceholder}" required></textarea><br/>
            <button type="submit"><fmt:message key="button.send"/></button>
        </form>
    </div>

    <div class="sidebar">
        <h3><fmt:message key="title.onlineUsers"/></h3>
        <c:choose>
            <c:when test="${empty onlineUsers}">
                <p class="meta"><fmt:message key="message.noUsers"/></p>
            </c:when>
            <c:otherwise>
                <ul>
                    <c:forEach var="u" items="${onlineUsers}">
                        <li>
                            <c:out value="${u.username}"/>
                            <span class="meta">(<fmt:formatDate value="${u.lastLoginAt}" pattern="HH:mm:ss"/>)</span>
                        </li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>

        <form method="post" action="${pageContext.request.contextPath}/logout.jsp">
            <input type="hidden" name="lang" value="${pageLang}">
            <button type="submit"><fmt:message key="button.logout"/></button>
        </form>
    </div>
</div>

<%-- ============ 消息翻译（实验扩展） ============ --%>
<%-- 点消息旁的 🌐 → 展开语言按钮 → 请求 /api/translate → 把返回的 HTML 片段
     插到这条消息下面的 .tr-slot 里。再点一次 🌐 收起。 --%>
<script>
(function () {
    var ctx = '${pageContext.request.contextPath}';
    var list = document.getElementById('msgList');
    if (!list) return;

    var LANGS = [['zh', '中文'], ['en', 'English'], ['ja', '日本語'], ['ko', '한국어']];

    // 事件委托：绑定在 <ul> 上，以后消息列表怎么变都不用重新绑
    list.addEventListener('click', function (ev) {
        var btn = ev.target.closest('.tr-btn');
        if (!btn) return;

        var slot = list.querySelector('.tr-slot[data-id="' + btn.dataset.id + '"]');
        if (!slot) return;

        if (slot.dataset.open) {          // 已经是展开状态 → 收起
            slot.dataset.open = '';
            slot.innerHTML = '';
            return;
        }
        slot.dataset.open = '1';
        slot.innerHTML = '<span class="meta">翻译成：</span>';

        LANGS.forEach(function (pair) {
            var b = document.createElement('button');
            b.type = 'button';
            b.textContent = pair[1];
            b.onclick = function () { requestTranslation(btn.dataset.id, pair[0], slot); };
            slot.appendChild(b);
        });
    });

    function requestTranslation(id, lang, slot) {
        slot.innerHTML = '<span class="meta">翻译中…</span>';
        fetch(ctx + '/api/translate?messageId=' + encodeURIComponent(id) + '&lang=' + encodeURIComponent(lang),
              {cache: 'no-store'})
            .then(function (r) { return r.text(); })
            .then(function (html) { slot.innerHTML = html; })
            .catch(function () { slot.innerHTML = '<span class="meta">翻译失败，请稍后重试</span>'; });
    }
})();
</script>
</body>
</html>
