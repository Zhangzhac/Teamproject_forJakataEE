(function () {
    var select = document.getElementById('voiceDuration');
    var status = document.getElementById('settingsStatus');
    try {
        var stored = Number(localStorage.getItem('chat-voice-duration'));
        select.value = [15,30,60,120].includes(stored) ? String(stored) : '30';
    } catch (e) { select.value = '30'; }
    select.addEventListener('change', function () {
        try { localStorage.setItem('chat-voice-duration', select.value); status.textContent = ChatI18n.t("ui45"); }
        catch (e) { status.textContent = ChatI18n.t("ui46"); }
    });
})();
// Independent, local microphone test: measures volume only; never uploads audio.
(function () {
    var tip = document.getElementById('micTestAnchor');
    var button = document.createElement('button'); button.type = 'button'; button.textContent = ChatI18n.t("ui47");
    var output = document.createElement('span'); output.setAttribute('role', 'status');
    tip.after(button); button.after(output);
    if (!window.isSecureContext || !navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) { button.disabled = true; output.textContent = ChatI18n.t("ui48"); return; }
    button.addEventListener('click', async function () {
        var stream, context, frame, timer;
        var cleanupDone = false;
        button.disabled = true;
        var ended = false;
        function cleanup() {
            ended = true;
            if (cleanupDone && !stream) return;
            cleanupDone = true;
            window.removeEventListener('pagehide', cleanup);
            clearTimeout(timer); cancelAnimationFrame(frame);
            if (stream) stream.getTracks().forEach(function (track) { track.stop(); });
            if (context) context.close().catch(function () {});
            button.disabled = false;
        }
        window.addEventListener('pagehide', cleanup, {once: true});
        try {
            output.textContent = ChatI18n.t("ui49");
            stream = await navigator.mediaDevices.getUserMedia({audio: true});
            if (ended) { cleanup(); return; }
            var Audio = window.AudioContext || window.webkitAudioContext;
            context = new Audio(); await context.resume();
            var analyser = context.createAnalyser(); analyser.fftSize = 2048;
            context.createMediaStreamSource(stream).connect(analyser);
            var values = new Float32Array(analyser.fftSize), peak = 0;
            function measure() {
                if (ended) return;
                analyser.getFloatTimeDomainData(values);
                var rms = Math.sqrt(values.reduce(function (sum, x) { return sum + x*x; }, 0) / values.length);
                peak = Math.max(peak, rms);
                output.textContent = ChatI18n.t("ui50") + (rms*100).toFixed(2) + ChatI18n.t("ui51");
                frame = requestAnimationFrame(measure);
            }
            measure();
            timer = setTimeout(function () {
                output.textContent = peak > 0.005 ? ChatI18n.t("ui52") + (peak*100).toFixed(2) + ChatI18n.t("ui53") : ChatI18n.t("ui54");
                cleanup();
            }, 5000);
        } catch (e) { output.textContent = ChatI18n.t("ui55") + e.name; cleanup(); }
    });
})();

(function () {
    var key = 'chat-voice-log:' + document.body.dataset.voiceScope;
    var log = document.getElementById('voiceLog');
    function render() {
        try { log.textContent = ChatI18n.log(localStorage.getItem(key) || '') || ChatI18n.t("ui56"); }
        catch (e) { log.textContent = ChatI18n.t("ui57"); }
    }
    window.addEventListener('storage', function (event) { if (event.key === key || event.key === null) render(); });
    document.getElementById('clearVoiceLog').addEventListener('click', function () {
        try { localStorage.removeItem(key); render(); }
        catch (e) { log.textContent = ChatI18n.t("ui58"); }
    });
    render();
})();
