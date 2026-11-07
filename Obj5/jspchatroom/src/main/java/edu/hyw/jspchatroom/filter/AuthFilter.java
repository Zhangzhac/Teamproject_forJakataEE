package edu.hyw.jspchatroom.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * AuthFilter
 * 保护 chat.jsp 与 logout.jsp；未登录则重定向 login.jsp；
 * 并记录最近活跃时间 lastActiveAt。
 */
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String user = (session == null) ? null : (String) session.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 记录最近活跃时间（页面顶部展示）
        session.setAttribute("lastActiveAt", System.currentTimeMillis());
        chain.doFilter(request, response);
    }
}

