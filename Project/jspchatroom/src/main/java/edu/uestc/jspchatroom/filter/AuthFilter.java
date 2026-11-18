package edu.uestc.jspchatroom.filter;

import edu.uestc.jspchatroom.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Requires a signed-in session for the protected page controllers. */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Cache-Control", "no-store");
        HttpSession session = req.getSession(false);

        Object current = (session == null) ? null : session.getAttribute("currentUser");
        if (!(current instanceof User user) || user.getId() <= 0) {
            if ("fetch".equals(req.getHeader("X-Chat-Request"))) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String lang = req.getParameter("lang");
            String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
            resp.sendRedirect(req.getContextPath() + "/login.jsp" + langQuery);
            return;
        }

        chain.doFilter(request, response);
    }
}
