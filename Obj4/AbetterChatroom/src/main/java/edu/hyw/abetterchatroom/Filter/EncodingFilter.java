package edu.hyw.abetterchatroom.Filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * EncodingFilter
 * 统一请求/响应的字符编码；
 * 在 POST 请求中对敏感词进行替换（如“去死”等）为 "**"。
 */
public class EncodingFilter implements Filter {
    private String charset = "UTF-8";

    // 敏感词列表（可按需扩展或改为从 context-param 读取）
    private final List<String> badWords = Arrays.asList("去死", "傻逼", "笨蛋","华为","小米");

    /**
     * 读取 web.xml 中的 charset 初始化参数。
     */
    @Override
    public void init(FilterConfig cfg) {
        String c = cfg.getInitParameter("charset");
        if (c != null && !c.isEmpty()) charset = c.trim();
    }

    /**
     * 设置统一编码；在 POST 时对 msg 执行关键词过滤，把结果放入 request attribute: filteredMsg。
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        // 统一字符集，避免乱码
        // TODO
        req.setCharacterEncoding(charset);
        resp.setCharacterEncoding(charset);
        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) resp;
        // 对 POST 的消息内容做关键词过滤
        // TODO
        if ("POST".equals(httpRequest.getMethod())) {
            String msg = httpRequest.getParameter("msg");
            if (msg != null) {
                for (String badWord : badWords) {
                    msg = msg.replace(badWord, "***");
                }
                httpRequest.setAttribute("filteredMsg", msg);
            }
        }
        // 放行
        // TODO
        chain.doFilter(req, resp);
    }
}

