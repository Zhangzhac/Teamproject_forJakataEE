(function () {
    var ctx = document.body.dataset.contextPath;
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
        refreshing = fetch(ctx + '/chat.jsp?lang=' + encodeURIComponent(document.documentElement.lang),
            {cache: 'no-store', headers: {'X-Chat-Request': 'fetch'}})
            .then(function (r) {
                if (r.status === 401 || r.redirected) { stopped = true; throw new Error(ChatI18n.t("ui59")); }
                if (!r.ok) throw new Error(ChatI18n.t("ui60"));
                return r.text();
            }).then(function (html) {
                var doc = new DOMParser().parseFromString(html, 'text/html');
                var next = doc.getElementById('msgList');
                if (!next) throw new Error(ChatI18n.t("ui60"));
                syncOnlineUsers(doc);
                ['totalMessageCount', 'recentMessageSummary'].forEach(function (id) {
                    document.getElementById(id).textContent = doc.getElementById(id).textContent;
                });
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
                document.getElementById('recentMessageSummary').hidden = ids.size === 0;
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
    var stateKey = 'chat-translations-v1:' + form.dataset.voiceScope;
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
