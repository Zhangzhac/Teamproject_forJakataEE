package edu.uestc.jspchatroom.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * EncodingFilter
 * 统一请求/响应的字符编码；
 * 在 POST 请求中对敏感词进行替换（如"去死"等）为"**"。
 */
public class EncodingFilter implements Filter {

    private String charset = "UTF-8";

    private final List<String> badWords = Arrays.asList("去死", "傻逼", "笨蛋");

    @Override
    public void init(FilterConfig cfg) {
        String c = cfg.getInitParameter("charset");
        if (c != null && !c.isBlank()) charset = c.trim();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        // 统一字符集，避免乱码
        req.setCharacterEncoding(charset);
        resp.setCharacterEncoding(charset);

        // 对 POST 的消息内容做关键词过滤
        if (req instanceof HttpServletRequest http && "POST".equalsIgnoreCase(http.getMethod())) {
            String msg = req.getParameter("msg");
            if (msg != null) {
                String filtered = msg;
                for (String bad : badWords) {
                    filtered = filtered.replace(bad, "**");
                }
                // 提供给后续 JSP 使用（不覆盖原始参数）
                req.setAttribute("filteredMsg", filtered);
            }
        }

        chain.doFilter(req, resp);
    }
}