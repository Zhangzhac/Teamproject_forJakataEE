// Shared translations for dynamic chat and voice controls.
(function () {
    var messages = {
  "ui0": {
    "zh": "语音脚本已加载；",
    "en": "Voice script loaded; "
  },
  "ui1": {
    "zh": "当前环境不支持语音输入，请在支持的浏览器中通过 localhost 或 HTTPS 打开；仍可打字。",
    "en": "Voice input is unavailable. Use a supported browser on localhost or HTTPS. You can still type."
  },
  "ui2": {
    "zh": "停止语音",
    "en": "Stop listening"
  },
  "ui3": {
    "zh": "🎤 语音输入",
    "en": "🎤 Voice input"
  },
  "ui4": {
    "zh": "达到 ",
    "en": "Reached "
  },
  "ui5": {
    "zh": " 秒时限，停止语音输入",
    "en": " second limit; stopping recognition"
  },
  "ui6": {
    "zh": "已达到语音时限，正在停止并等待最后的识别结果…",
    "en": "Time limit reached. Stopping and waiting for final results…"
  },
  "ui7": {
    "zh": "语音已超时停止，已确认的文字保留在输入框。",
    "en": "Voice input timed out. Confirmed text remains in your draft."
  },
  "ui8": {
    "zh": "请求开始识别；语言 zh-CN",
    "en": "Requesting recognition; language zh-CN"
  },
  "ui9": {
    "zh": "已启用麦克风，但 15 秒内未收到最终文字；请到设置页查看诊断记录，可能是收音或在线识别服务问题。",
    "en": "Microphone enabled, but no final text after 15 seconds. Check diagnostics in Settings for audio or service issues."
  },
  "ui10": {
    "zh": "15 秒内未开始收音；请检查 Chrome 麦克风设置和识别服务。",
    "en": "Audio capture did not start within 15 seconds. Check microphone settings and the recognition service."
  },
  "ui11": {
    "zh": "等待超时（未中断识别）；audio=",
    "en": "Result wait timed out (recognition continues); audio="
  },
  "ui12": {
    "zh": "启动异常：",
    "en": "Start exception: "
  },
  "ui13": {
    "zh": "语音启动失败：",
    "en": "Could not start voice input: "
  },
  "ui14": {
    "zh": "start：识别服务启动",
    "en": "start: recognition started"
  },
  "ui15": {
    "zh": "识别已启动，请说普通话；本次将在 ",
    "en": "Recognition started. Speak Mandarin; automatic stop within "
  },
  "ui16": {
    "zh": " 秒内自动停止。",
    "en": " seconds."
  },
  "ui17": {
    "zh": "audiostart：开始采集音频",
    "en": "audiostart: capturing audio"
  },
  "ui18": {
    "zh": "正在收音，等待识别结果…",
    "en": "Capturing audio; waiting for recognition…"
  },
  "ui19": {
    "zh": "soundstart：检测到声音",
    "en": "soundstart: sound detected"
  },
  "ui20": {
    "zh": "检测到声音，等待识别服务返回文字…",
    "en": "Sound detected; waiting for text…"
  },
  "ui21": {
    "zh": "speechstart：检测到语音",
    "en": "speechstart: speech detected"
  },
  "ui22": {
    "zh": "检测到语音，正在识别…",
    "en": "Speech detected; recognizing…"
  },
  "ui23": {
    "zh": "nomatch：无法匹配文字",
    "en": "nomatch: no matching text"
  },
  "ui24": {
    "zh": "已收到音频，但未识别出文字，请重试。",
    "en": "Audio received, but no text recognized. Try again."
  },
  "ui25": {
    "zh": "audioend：音频采集结束",
    "en": "audioend: audio capture ended"
  },
  "ui26": {
    "zh": "result：收到识别结果",
    "en": "result: recognition result received"
  },
  "ui27": {
    "zh": "已达到 500 字限制，请编辑后发送。",
    "en": "The 500-character limit has been reached. Edit before sending."
  },
  "ui28": {
    "zh": "识别内容超过 500 字，已截断，请检查后发送。",
    "en": "Recognized text exceeded 500 characters and was truncated. Review before sending."
  },
  "ui29": {
    "zh": "识别中：",
    "en": "Recognizing: "
  },
  "ui30": {
    "zh": "已写入输入框，可继续说话或停止后编辑。",
    "en": "Text added to your draft. Continue speaking, or stop to edit."
  },
  "ui31": {
    "zh": "麦克风权限被拒绝，请在浏览器站点设置中允许。",
    "en": "Microphone permission denied. Allow access in browser site settings."
  },
  "ui32": {
    "zh": "浏览器语音识别服务不可用。",
    "en": "The browser recognition service is unavailable."
  },
  "ui33": {
    "zh": "未检测到麦克风。",
    "en": "No microphone detected."
  },
  "ui34": {
    "zh": "未听到语音，请重试。",
    "en": "No speech detected. Try again."
  },
  "ui35": {
    "zh": "Chrome 在线语音识别服务连接失败（network）。麦克风权限正常也可能发生；请检查服务网络连通性，或换浏览器测试。",
    "en": "The online recognition service could not connect (network). Check connectivity or try another browser. Microphone permission alone is not sufficient."
  },
  "ui36": {
    "zh": "识别服务不支持普通话 zh-CN。",
    "en": "The service does not support Mandarin (zh-CN)."
  },
  "ui37": {
    "zh": "语音识别已取消。",
    "en": "Voice recognition cancelled."
  },
  "ui38": {
    "zh": "语音识别失败，请重试。",
    "en": "Voice recognition failed. Try again."
  },
  "ui39": {
    "zh": "end：识别结束；final=",
    "en": "end: recognition ended; final="
  },
  "ui40": {
    "zh": "已达到语音时限，识别文字已保留，可检查后发送。",
    "en": "Time limit reached. Recognized text has been kept for review."
  },
  "ui41": {
    "zh": "语音输入已结束，可检查文字后发送。",
    "en": "Voice input ended. Review the text before sending."
  },
  "ui42": {
    "zh": "只有临时识别结果，没有最终文字，请重试。",
    "en": "Only interim results were received; no final text. Try again."
  },
  "ui43": {
    "zh": "未收到任何识别文字。请查看诊断记录；若没有声音事件，可到设置页检测麦克风输入。",
    "en": "No recognition text received. Check diagnostics and microphone input in Settings."
  },
  "ui44": {
    "zh": "语音输入已停止，草稿已保留。",
    "en": "Voice input stopped. Your draft has been kept."
  },
  "ui45": {
    "zh": "已保存，下次启动语音输入时生效。",
    "en": "Saved. Applies the next time you start voice input."
  },
  "ui46": {
    "zh": "浏览器不允许保存设置，将使用默认 30 秒。",
    "en": "Settings cannot be saved. The default of 30 seconds will be used."
  },
  "ui47": {
    "zh": "检测麦克风音量（5 秒）",
    "en": "Test microphone volume (5 seconds)"
  },
  "ui48": {
    "zh": " 需要 localhost 或 HTTPS 以及麦克风支持。",
    "en": " Requires localhost or HTTPS and microphone support."
  },
  "ui49": {
    "zh": " 请先停止语音识别，再允许麦克风权限并说话…",
    "en": " Stop voice recognition first, then allow microphone access and speak…"
  },
  "ui50": {
    "zh": " 本地输入电平：",
    "en": " Local input level: "
  },
  "ui51": {
    "zh": "%（请说话）",
    "en": "% (please speak)"
  },
  "ui52": {
    "zh": " 检测到音频输入（峰值 ",
    "en": " Audio input detected (peak "
  },
  "ui53": {
    "zh": "%）。这仅说明设备有声音，不代表识别服务可用。",
    "en": "%). This confirms audio input, not recognition service availability."
  },
  "ui54": {
    "zh": " 输入电平很低。请检查 Chrome 选中的麦克风、Windows 输入音量及静音开关。",
    "en": " Input level is very low. Check your browser microphone selection, Windows input volume and mute switch."
  },
  "ui55": {
    "zh": " 麦克风检测失败：",
    "en": " Microphone test failed: "
  },
  "ui56": {
    "zh": "暂无诊断记录，请在聊天室启动语音输入。",
    "en": "No diagnostics yet. Start voice input in the chatroom."
  },
  "ui57": {
    "zh": "浏览器禁止访问本地存储，无法读取诊断记录。",
    "en": "Local storage is blocked. Diagnostics cannot be read."
  },
  "ui58": {
    "zh": "无法清空记录，请检查浏览器存储权限。",
    "en": "Unable to clear diagnostics. Check browser storage permissions."
  },
  "ui59": {
    "zh": "登录已失效，请重新登录",
    "en": "Your session has expired. Please sign in again."
  },
  "ui60": {
    "zh": "消息更新失败，将自动重试",
    "en": "Could not update messages. Retrying automatically."
  },
  "ui61": {
    "zh": "发送中…",
    "en": "Sending…"
  },
  "ui62": {
    "zh": "发送未确认，请检查消息列表后重试",
    "en": "Send was not confirmed. Check the message list before retrying."
  },
  "ui63": {
    "zh": "原文 / Source: ",
    "en": "Source: "
  },
  "ui64": {
    "zh": "自动判断 / Auto",
    "en": "Auto-detect"
  },
  "ui65": {
    "zh": "翻译中 / Translating…",
    "en": "Translating…"
  },
  "ui66": {
    "zh": "翻译失败，请重试 / Translation failed; retry",
    "en": "Translation failed. Please retry."
  }
};
    messages['language.zh'] = {zh:'中文',en:'Chinese'};
    messages['language.en'] = {zh:'English',en:'English'};
    messages['language.ja'] = {zh:'日本語',en:'Japanese'};
    messages['language.ko'] = {zh:'한국어',en:'Korean'};
    var english = document.documentElement.lang.toLowerCase().startsWith('en');
    window.ChatI18n = {
        t: function (key) { return messages[key] ? messages[key][english ? 'en' : 'zh'] : key; },
        log: function (text) {
            var pairs = Object.values(messages).slice().sort(function (a,b) { return b.zh.length - a.zh.length; });
            if (english) pairs.forEach(function (p) { text = text.split(p.zh).join(p.en); });
            if (!english) Object.values(messages).slice().sort(function (a,b) { return b.en.length - a.en.length; }).forEach(function (p) {
                if (p.en.length > 12) text = text.split(p.en).join(p.zh);
            });
            return text;
        }
    };
})();
