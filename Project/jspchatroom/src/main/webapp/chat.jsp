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
        if ("fetch".equals(request.getHeader("X-Chat-Request"))) {
            response.setStatus(204);
            return;
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
    | <fmt:message key="label.onlineUsers"/>: <span id="onlineUserCount">${onlineUserCount}</span>
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
                <p class="meta" id="emptyMessages"><fmt:message key="message.noMessages"/></p>
                <ul class="msg" id="msgList"></ul>
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
                            <button type="button" class="tr-btn" data-id="${m.id}" title="<fmt:message key="extension.label4"/>">🌐</button>
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
        <div id="onlineUserList" aria-live="polite">
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
        </div>

        <form method="post" action="${pageContext.request.contextPath}/logout.jsp">
            <input type="hidden" name="lang" value="${pageLang}">
            <button type="submit"><fmt:message key="button.logout"/></button>
        </form>
    </div>
</div>

<%-- ============ 消息翻译（实验扩展） ============ --%>
<%-- 点消息旁的 🌐 → 展开语言按钮 → 请求 /api/translate → 把返回的 HTML 片段
     插到这条消息下面的 .tr-slot 里。再点一次 🌐 收起。 --%>
<script src="${pageContext.request.contextPath}/chat-i18n.js?v=1"></script>
<script>
(function () {
    var ctx = '${pageContext.request.contextPath}';
    var list = document.getElementById('msgList');
    if (!list) return;

    var form = document.getElementById('messageForm');
    var input = form.querySelector('textarea');
    var send = form.querySelector('button[type="submit"]');
    var status = document.createElement('span');
    status.className = 'meta'; status.setAttribute('role', 'status');
    form.appendChild(status);
    var refreshing = null;
    var stopped = false;
    var sending = false;

    // Update only presence display; leave logout form and composer untouched.
    function syncOnlineUsers(doc) {
        ['onlineUserList', 'onlineUserCount'].forEach(function (id) {
            var current = document.getElementById(id);
            var latest = doc.getElementById(id);
            if (!current || !latest) return;
            if (id === 'onlineUserCount') {
                current.textContent = latest.textContent;
            } else if (current.innerHTML !== latest.innerHTML) {
                // HTML is produced by this app's JSP; usernames use c:out escaping.
                current.replaceChildren.apply(current, Array.from(latest.childNodes).map(function (node) {
                    return document.importNode(node, true);
                }));
            }
        });
    }

    // Preserve existing message elements, including translations and active requests.
    function refreshMessages() {
        if (refreshing) return refreshing;
        refreshing = fetch(ctx + '/chat.jsp' + window.location.search,
            {cache: 'no-store', headers: {'X-Chat-Request': 'fetch'}})
            .then(function (r) {
                if (r.redirected) { stopped = true; throw new Error(ChatI18n.t("ui59")); }
                if (!r.ok) throw new Error(ChatI18n.t("ui60"));
                return r.text();
            }).then(function (html) {
                var doc = new DOMParser().parseFromString(html, 'text/html');
                var next = doc.getElementById('msgList');
                if (!next) throw new Error(ChatI18n.t("ui60"));
                syncOnlineUsers(doc);
                var ids = new Set();
                var nearBottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 100;
                var added = false;
                next.querySelectorAll('li[data-id]').forEach(function (item) {
                    var id = item.dataset.id; ids.add(id);
                    if (!list.querySelector('li[data-id="' + id + '"]')) {
                        list.appendChild(document.importNode(item, true)); added = true;
                    }
                });
                list.querySelectorAll('li[data-id]').forEach(function (item) {
                    if (!ids.has(item.dataset.id)) {
                        item.remove(); delete saved[item.dataset.id];
                    }
                });
                persist();
                var empty = document.getElementById('emptyMessages');
                if (empty) empty.hidden = ids.size > 0;
                if (added && nearBottom && document.activeElement !== input) list.lastElementChild.scrollIntoView({block: 'nearest'});
                if (!sending) status.textContent = '';
            }).finally(function () { refreshing = null; });
        return refreshing;
    }
    function poll() {
        if (stopped) return;
        refreshMessages().catch(function (e) { status.textContent = e.message; })
            .finally(function () { if (!stopped) window.setTimeout(poll, 2000); });
    }
    window.setTimeout(poll, 2000);
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        if (sending || stopped || !input.value.trim()) return;
        sending = true; send.disabled = true;
        var draft = input.value;
        status.textContent = ChatI18n.t("ui61");
        fetch(form.action, {method: 'POST', headers: {'X-Chat-Request': 'fetch'},
            body: new URLSearchParams(new FormData(form))})
            .then(function (r) {
                if (r.status !== 204) throw new Error(ChatI18n.t("ui62"));
                if (input.value === draft) input.value = '';
                // Wait for an older poll before reading the newly committed message.
                return Promise.resolve(refreshing).catch(function () {}).then(refreshMessages);
            }).catch(function (e) { status.textContent = e.message; })
            .finally(function () { sending = false; send.disabled = false; });
    });

    // Keep only UI preferences, scoped to this tab, app and signed-in user.
    var stateKey = 'chat-translations-v1:' + ctx + ':${currentUser.id}';
    var saved = {};
    try { saved = JSON.parse(sessionStorage.getItem(stateKey) || '{}'); } catch (e) {}
    if (!saved || typeof saved !== 'object' || Array.isArray(saved)) saved = {};
    function persist() {
        try { sessionStorage.setItem(stateKey, JSON.stringify(saved)); } catch (e) {}
    }
    // Drop preferences for messages no longer in the displayed recent-message list.
    Object.keys(saved).forEach(function (id) {
        if (!/^\d+$/.test(id) || !list.querySelector('.tr-slot[data-id="' + id + '"]')) delete saved[id];
    });
    persist();

    var LANGS = [['zh', ChatI18n.t('language.zh')], ['en', ChatI18n.t('language.en')], ['ja', ChatI18n.t('language.ja')], ['ko', ChatI18n.t('language.ko')]];

    // 事件委托：绑定在 <ul> 上，以后消息列表怎么变都不用重新绑
    list.addEventListener('click', function (ev) {
        var btn = ev.target.closest('.tr-btn');
        if (!btn) return;

        var slot = list.querySelector('.tr-slot[data-id="' + btn.dataset.id + '"]');
        if (!slot) return;

        if (slot.dataset.open) {          // 已经是展开状态 → 收起
            slot._version = (slot._version || 0) + 1;
            delete saved[btn.dataset.id];
            persist();
            slot.dataset.open = '';
            slot.innerHTML = '';
            return;
        }
        slot.dataset.open = '1';
        saved[btn.dataset.id] = saved[btn.dataset.id] || {source: 'auto'};
        persist();
        slot.innerHTML = "<span class=\"meta\">" + ChatI18n.t("ui63") + "</span>";
        var source = document.createElement('select');
        [['auto',ChatI18n.t("ui64")], ['zh',ChatI18n.t('language.zh')], ['en',ChatI18n.t('language.en')], ['ja',ChatI18n.t('language.ja')], ['ko',ChatI18n.t('language.ko')]].forEach(function (p) {
            var o = document.createElement('option'); o.value=p[0]; o.textContent=p[1]; source.appendChild(o);
        });
        source.value = saved[btn.dataset.id].source || 'auto';
        source.onchange = function () {
            saved[btn.dataset.id] = {source: source.value}; persist();
        };
        slot.appendChild(source);
        slot.appendChild(document.createTextNode(' → '));

        LANGS.forEach(function (pair) {
            var b = document.createElement('button');
            b.type = 'button';
            b.textContent = pair[1];
            b.onclick = function () { requestTranslation(btn.dataset.id, pair[0], slot, source.value); };
            slot.appendChild(b);
        });
    });

    // Reopen selected messages and read their translations from the server cache.
    Object.keys(saved).forEach(function (id) {
        var preference = saved[id];
        var button = list.querySelector('.tr-btn[data-id="' + id + '"]');
        if (!button || !preference || typeof preference !== 'object') return;
        if (['auto','zh','en','ja','ko'].indexOf(preference.source) < 0) preference.source = 'auto';
        button.click();
        if (['zh','en','ja','ko'].indexOf(preference.lang) >= 0) {
            requestTranslation(id, preference.lang, list.querySelector('.tr-slot[data-id="' + id + '"]'), preference.source);
        }
    });

    function requestTranslation(id, lang, slot, source) {
        saved[id] = {source: source, lang: lang};
        persist();
        var version = slot._version = (slot._version || 0) + 1;
        slot.innerHTML = "<span class=\"meta\">" + ChatI18n.t("ui65") + "</span>";
        fetch(ctx + '/api/translate?messageId=' + encodeURIComponent(id) + '&lang=' + encodeURIComponent(lang) + '&source=' + encodeURIComponent(source) + '&ui=' + encodeURIComponent(document.documentElement.lang),
              {cache: 'no-store'})
            .then(function (r) {
                return r.text().then(function (html) {
                    if (!r.ok) {
                        if (r.status === 400 || r.status === 401 || r.status === 404 || r.status === 502) return html;
                        throw new Error('HTTP ' + r.status);
                    }
                    return html;
                });
            })
            .then(function (html) { if (slot.dataset.open && slot._version === version) slot.innerHTML = html; })
            .catch(function () { if (slot.dataset.open && slot._version === version) slot.innerHTML = "<span class=\"meta\">" + ChatI18n.t("ui66") + "</span>"; });
    }
})();
</script>
<script src="${pageContext.request.contextPath}/voice-input.js?v=5"></script>
</body>
</html>
