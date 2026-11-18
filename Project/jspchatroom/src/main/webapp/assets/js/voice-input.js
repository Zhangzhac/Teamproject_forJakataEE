(function () {
    'use strict';
    var form = document.getElementById('messageForm');
    var input = form.querySelector('textarea');
    var mic = document.getElementById('micBtn');
    var auto = document.getElementById('voiceAutoSend');
    var tip = document.getElementById('voiceTip');
    var logKey = 'chat-voice-log:' + form.dataset.voiceScope;
    var fallbackLog = '';
    function trace(message) {
        var previous = fallbackLog;
        try { previous = localStorage.getItem(logKey) || ''; } catch (e) {}
        fallbackLog = (previous + new Date().toLocaleTimeString() + ' ' + message + '\n')
            .split('\n').filter(Boolean).slice(-30).join('\n') + '\n';
        try { localStorage.setItem(logKey, fallbackLog); } catch (e) {}
    }
    trace(ChatI18n.t("ui0") + navigator.userAgent);
    var SR = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SR || !window.isSecureContext) {
        mic.disabled = true;
        tip.textContent = ChatI18n.t("ui1");
        return;
    }
    var recognition = new SR();
    recognition.lang = 'zh-CN';
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.maxAlternatives = 1;
    var active = false, starting = false, failed = false, cancelAuto = false, recognized = false;
    var stopTimer, releaseTimer, timedOut = false;
    function durationSeconds() {
        try { var value = Number(localStorage.getItem('chat-voice-duration')); if ([15,30,60,120].includes(value)) return value; } catch (e) {}
        return 30;
    }
    function clearStopTimers() { clearTimeout(stopTimer); clearTimeout(releaseTimer); }
    function hint(text) { tip.textContent = text; }
    function state(on) {
        active = on; starting = false;
        mic.textContent = on ? ChatI18n.t("ui2") : ChatI18n.t("ui3");
        mic.setAttribute('aria-pressed', String(on));
    }
    mic.addEventListener('click', function () {
        if (starting) return;
        if (active) { recognition.stop(); return; }
        failed = false; cancelAuto = false; recognized = false; timedOut = false;
        clearStopTimers();
        var duration = durationSeconds();
        stopTimer = setTimeout(function () {
            if (!active && !starting) return;
            timedOut = true;
            trace(ChatI18n.t("ui4") + duration + ChatI18n.t("ui5"));
            hint(ChatI18n.t("ui6"));
            if (active) recognition.stop(); else recognition.abort();
            // Some engines delay end; abort releases capture if graceful stop stalls.
            releaseTimer = setTimeout(function () {
                if (active || starting) { cancelAuto = true; recognition.abort(); state(false); hint(ChatI18n.t("ui7")); }
            }, 3000);
        }, duration * 1000);
        audioStarted = soundDetected = speechDetected = gotResult = false;
        trace(ChatI18n.t("ui8"));
        clearTimeout(watchdog);
        watchdog = setTimeout(function () {
            if (!recognized && (active || starting)) {
                hint(audioStarted ? ChatI18n.t("ui9") : ChatI18n.t("ui10"));
                trace(ChatI18n.t("ui11") + audioStarted + ', sound=' + soundDetected + ', speech=' + speechDetected + ', result=' + gotResult);
            }
        }, 15000);
        starting = true;
        try { recognition.start(); }
        catch (e) { clearStopTimers(); clearTimeout(watchdog); starting = false; trace(ChatI18n.t("ui12") + e.name); hint(ChatI18n.t("ui13") + e.name); }
    });
    var audioStarted = false, soundDetected = false, speechDetected = false, gotResult = false;
    var watchdog;
    recognition.onstart = function () {
        state(true); trace(ChatI18n.t("ui14"));
        hint(ChatI18n.t("ui15") + durationSeconds() + ChatI18n.t("ui16"));
    };
    recognition.onaudiostart = function () { audioStarted = true; trace(ChatI18n.t("ui17")); hint(ChatI18n.t("ui18")); };
    recognition.onsoundstart = function () { soundDetected = true; trace(ChatI18n.t("ui19")); hint(ChatI18n.t("ui20")); };
    recognition.onspeechstart = function () { speechDetected = true; trace(ChatI18n.t("ui21")); hint(ChatI18n.t("ui22")); };
    recognition.onnomatch = function () { trace(ChatI18n.t("ui23")); hint(ChatI18n.t("ui24")); };
    recognition.onaudioend = function () { trace(ChatI18n.t("ui25")); };
    recognition.onresult = function (event) {
        if (cancelAuto) return;
        gotResult = true; trace(ChatI18n.t("ui26"));
        var interim = '';
        for (var i = event.resultIndex; i < event.results.length; i++) {
            var result = event.results[i];
            if (result.isFinal) {
                var text = result[0].transcript.trim();
                if (!text) continue;
                var remaining = input.maxLength - input.value.length;
                if (remaining <= 0) { cancelAuto = true; recognition.stop(); hint(ChatI18n.t("ui27")); return; }
                // Append to the current draft, so typing during recognition is preserved.
                input.value += text.slice(0, remaining);
                recognized = true;
                if (text.length > remaining) {
                    cancelAuto = true; recognition.stop(); hint(ChatI18n.t("ui28")); return;
                }
            } else interim += result[0].transcript;
        }
        hint(interim ? ChatI18n.t("ui29") + interim : ChatI18n.t("ui30"));
    };
    recognition.onerror = function (event) {
        failed = true; clearTimeout(stopTimer); clearTimeout(watchdog); trace('error：' + event.error);
        hint({'not-allowed':ChatI18n.t("ui31"),
            'service-not-allowed':ChatI18n.t("ui32"), 'audio-capture':ChatI18n.t("ui33"),
            'no-speech':ChatI18n.t("ui34"), 'network':ChatI18n.t("ui35"),
            'language-not-supported':ChatI18n.t("ui36"), 'aborted':ChatI18n.t("ui37")}[event.error] || ChatI18n.t("ui38"));
    };
    recognition.onend = function () {
        clearStopTimers(); clearTimeout(watchdog); trace(ChatI18n.t("ui39") + recognized + ', result=' + gotResult);
        state(false);
        if (!failed && !cancelAuto) hint(recognized ? (timedOut ? ChatI18n.t("ui40") : ChatI18n.t("ui41")) : gotResult ? ChatI18n.t("ui42") : ChatI18n.t("ui43"));
        if (auto.checked && recognized && !failed && !cancelAuto && input.value.trim()) form.requestSubmit();
    };
    // Stop recognition before sending, including auto-send; never bypass the AJAX handler.
    form.addEventListener('submit', function () {
        cancelAuto = true;
        if (active || starting) recognition.abort();
    }, true);
    document.addEventListener('keydown', function (event) {
        if (event.repeat) return;
        if (event.altKey && event.key.toLowerCase() === 'v') { event.preventDefault(); mic.click(); }
        if (event.key === 'Escape' && (active || starting)) {
            cancelAuto = true; recognition.abort(); hint(ChatI18n.t("ui44"));
        }
    });
    window.addEventListener('pagehide', function () { clearStopTimers(); clearTimeout(watchdog); cancelAuto = true; if (active || starting) recognition.abort(); });
})();
