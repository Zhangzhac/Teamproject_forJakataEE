package edu.hyw.jspchatroom.web;

import edu.hyw.jspchatroom.dao.TranslationDao;
import edu.hyw.jspchatroom.service.MyMemoryProvider;
import edu.hyw.jspchatroom.service.TranslationProvider;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * TranslateServlet —— 消息翻译接口（实验扩展）。
 *
 * <p>请求：{@code GET /api/translate?messageId=12&lang=zh}</p>
 *
 * <p>返回的是<b>一小段 HTML</b>而不是 JSON：</p>
 * <pre>
 *   &lt;div class="translation"&gt;&lt;span class="tr-lang"&gt;[中文]&lt;/span&gt; 译文内容&lt;/div&gt;
 * </pre>
 * <p>这样前端拿到直接用 {@code slot.innerHTML = html} 塞进去就行，
 * 整个项目不需要引入任何 JSON 库。</p>
 *
 * <p>处理流程：</p>
 * <ol>
 *   <li>参数校验（语言必须在白名单里，消息编号必须是数字）；</li>
 *   <li>先查 translation 缓存，命中就直接返回，<b>不调用外部接口</b>；</li>
 *   <li>没命中：取原文 → 调翻译引擎 → 写缓存 → 返回。</li>
 * </ol>
 */
@WebServlet("/api/translate")
public class TranslateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** 支持的语言：中文、英文、日文、韩文。 */
    private static final Map<String, String> LANG_NAMES = new LinkedHashMap<>();

    static {
        LANG_NAMES.put("zh", "中文");
        LANG_NAMES.put("en", "English");
        LANG_NAMES.put("ja", "日本語");
        LANG_NAMES.put("ko", "한국어");
    }

    private static final Set<String> SUPPORTED = LANG_NAMES.keySet();

    private final TranslationDao translationDao = new TranslationDao();

    /** 翻译引擎：想换成百度/有道，只改这一行。 */
    private final MyMemoryProvider provider = new MyMemoryProvider();
    // Fixed lock stripes bound memory and serialize cache misses for each key.
    private final Object[] locks = java.util.stream.IntStream.range(0, 64).mapToObj(i -> new Object()).toArray();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setLocale("en".equals(request.getParameter("ui")) ? java.util.Locale.ENGLISH : java.util.Locale.CHINESE);
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        if (request.getSession(false) == null || !(request.getSession(false).getAttribute("currentUser") instanceof edu.hyw.jspchatroom.model.User user) || user.getId() <= 0) {
            response.setStatus(401);
            writeError(response, "请先登录 / Please sign in");
            return;
        }
        String lang = request.getParameter("lang");
        String idParam = request.getParameter("messageId");

        if (lang == null || !SUPPORTED.contains(lang) || idParam == null || idParam.isBlank()) {
            response.setStatus(400);
            writeError(response, "参数不完整，需要 messageId 和 lang");
            return;
        }

        int messageId;
        try {
            messageId = Integer.parseInt(idParam.trim());
            if (messageId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.setStatus(400);
            writeError(response, "消息编号无效");
            return;
        }

        try {
            String source = translationDao.findMessageContent(messageId);
            if (source == null) { response.setStatus(404); writeError(response, "消息不存在 / Message not found"); return; }
            String from = request.getParameter("source");
            if (from == null || from.equals("auto")) from = MyMemoryProvider.detectLanguage(source);
            if (!SUPPORTED.contains(from)) throw new IllegalArgumentException("原文语言无效 / Invalid source language");
            // Versioned key bypasses potentially invalid legacy translations without deleting data.
            String cacheKey = "v2:" + from + ":" + lang;
            synchronized (locks[(cacheKey.hashCode() ^ messageId) & 63]) {
            // Cache lookup and provider request share the same lock.
            String cached = translationDao.findTranslation(messageId, cacheKey);
            if (cached != null) {
                writeTranslation(response, lang, cached);
                return;
            }

            String translated = provider.translate(source, lang, from);

            // 3. 写缓存后再返回（写失败不影响本次显示，所以单独 try）
            try {
                translationDao.saveTranslation(messageId, cacheKey, translated);
            } catch (Exception e) {
                System.err.println("[TranslateServlet] 翻译结果没存进缓存: " + e.getMessage());
            }
            writeTranslation(response, lang, translated);
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(400);
            writeError(response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(502);
            writeError(response, "翻译暂不可用，请稍后重试 / Translation unavailable; retry later");
        }
    }

    /** 输出译文片段。 */
    private void writeTranslation(HttpServletResponse response, String lang, String text) throws IOException {
        response.getWriter().write("<div class=\"translation\">"
                + "<span class=\"tr-lang\">[" + escapeHtml("en".equals(response.getLocale().getLanguage()) ? Map.of("zh", "Chinese", "en", "English", "ja", "Japanese", "ko", "Korean").getOrDefault(lang, lang) : LANG_NAMES.getOrDefault(lang, lang)) + "]</span> "
                + escapeHtml(text)
                + "</div>");
    }

    /** 输出错误片段（前端直接显示在消息下面）。 */
    private void writeError(HttpServletResponse response, String message) throws IOException {
        boolean english = "en".equals(response.getLocale().getLanguage());
        if (message.contains(" / ")) {
            String[] parts = message.split(" / ", 2);
            message = parts[english ? 1 : 0];
        } else if (english) {
            message = switch (message) {
                case "参数不完整，需要 messageId 和 lang" -> "Missing or invalid messageId or target language.";
                case "消息编号无效" -> "Invalid message ID.";
                default -> "Translation request failed. Please retry.";
            };
        }
        response.getWriter().write("<div class=\"translation err\">" + escapeHtml(message) + "</div>");
    }

    /** HTML 转义：译文来自外部接口，必须转义后再写进页面。 */
    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
