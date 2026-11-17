package edu.hyw.jspchatroom.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * MyMemoryProvider —— 基于 MyMemory 免费翻译 API 的实现。
 *
 * <p>接口地址：{@code https://api.mymemory.translated.net/get?q=...&langpair=en|zh-CN}</p>
 *
 * <p>选它的理由：<b>不需要注册、不需要申请 key</b>，拿来就能跑，
 * 适合先验证整个流程；缺点是每天有匿名额度限制，而且<b>不能自动检测源语言</b>，
 * 必须明确告诉它 langpair（所以见 {@link #langPair(String)} 的简单规则）。</p>
 *
 * <p>HTTP 客户端用 JDK 自带的 {@link HttpClient}（Java 11+），
 * 不需要引入任何第三方库。</p>
 */
public class MyMemoryProvider implements TranslationProvider {

    /** 免费接口地址。 */
    private static final String ENDPOINT = "https://api.mymemory.translated.net/get";

    /** 复用同一个客户端（内部有连接池，每次 new 反而更慢）。 */
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String translate(String text, String targetLang) throws Exception {
        String pair = langPair(targetLang);
        String url = ENDPOINT
                + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                + "&langpair=" + URLEncoder.encode(pair, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "jspchatroom-lab/1.0")
                .GET()
                .build();
        HttpResponse<String> response =
                HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new IOException("翻译服务返回 HTTP " + response.statusCode());
        }

        String body = response.body();
        String translated = readJsonString(body, "translatedText");
        if (translated == null || translated.isBlank()) {
            String status = readJsonString(body, "responseStatus");
            throw new IOException("翻译服务没有返回结果"
                    + (status == null ? "" : "（响应状态：" + status + "）"));
        }
        // 该服务偶尔把 HTML 实体直接塞进译文里，顺手还原
        return translated.replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&");
    }

    /**
     * 根据目标语言反推源语言。
     *
     * <p>MyMemory 不支持 autodetect，聊天室里主要是中英文混着说，
     * 这里用最简单的规则：翻成中文时按“英文原文”处理，翻成其它语言时按“中文原文”处理。
     * 如果以后要更准，可以换成支持 {@code auto} 检测的百度翻译。</p>
     */
    private String langPair(String targetLang) {
        if ("zh".equals(targetLang)) {
            return "en|zh-CN";
        }
        if ("en".equals(targetLang)) {
            return "zh-CN|en";
        }
        return "zh-CN|" + targetLang;
    }

    /**
     * 从 JSON 文本里取某个字段的字符串值。
     *
     * <p>只为这一个字段引入整个 JSON 库不划算，这里手写一个够用的解析：
     * 找到 {@code "key"}，跳到后面的冒号，再逐个字符读字符串字面量，
     * 完整处理引号、反斜杠、斜杠、换行、制表符和 Unicode 转义（反斜杠+u+四位十六进制）。</p>
     *
     * @return 字段值；字段不存在或格式不对时返回 {@code null}
     */
    private String readJsonString(String json, String key) {
        String marker = "\"" + key + "\"";
        int keyIndex = json.indexOf(marker);
        if (keyIndex < 0) {
            return null;
        }
        int colon = json.indexOf(':', keyIndex + marker.length());
        if (colon < 0) {
            return null;
        }
        int quote = json.indexOf('"', colon + 1);
        if (quote < 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = quote + 1; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"') {
                return sb.toString();
            }
            if (ch != '\\') {
                sb.append(ch);
                continue;
            }
            i++;
            if (i >= json.length()) {
                break;
            }
            char esc = json.charAt(i);
            switch (esc) {
                case '"' -> sb.append('"');
                case '\\' -> sb.append('\\');
                case '/' -> sb.append('/');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'u' -> {
                    if (i + 4 < json.length()) {
                        try {
                            sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                            i += 4;
                        } catch (NumberFormatException ignore) {
                            // Unicode 转义后面不是合法十六进制，原样丢掉
                        }
                    }
                }
                default -> sb.append(esc);
            }
        }
        return null;
    }
}
