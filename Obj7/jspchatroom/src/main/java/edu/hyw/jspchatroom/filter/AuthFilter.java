package edu.hyw.jspchatroom.filter;

import edu.hyw.jspchatroom.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * AuthFilter
 * 保护聊天页与退出页（web.xml 中映射到 /chat.jsp 与 /logout.jsp）；
 * 未登录则重定向到 login.jsp，并记录本次活跃时间 lastActiveAt。
 *
 * <p>登录成功后由 login.jsp 把 {@link User} 对象放进 session 的
 * {@code currentUser} 属性，这里就凭它判断登录状态 —— 也就是说
 * <b>登录状态来自数据库校验的结果</b>，而不是页面自己随便设个用户名。</p>
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        Object current = (session == null) ? null : session.getAttribute("currentUser");
        if (!(current instanceof User user) || user.getId() <= 0) {
            // 未登录（或登录信息已失效）：带着原始语言回到登录页
            String lang = req.getParameter("lang");
            String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
            resp.sendRedirect(req.getContextPath() + "/login.jsp" + langQuery);
            return;
        }

        // 记录最近活跃时间（页面顶部展示）
        session.setAttribute("lastActiveAt", System.currentTimeMillis());
        chain.doFilter(request, response);
    }
}
