package edu.hyw.abetterchatroom;


import jakarta.servlet.*;
        import jakarta.servlet.http.*;

        import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * LogoutServlet
 * 退出登录：从在线列表移除并使会话失效。
 */
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("user");
            if (username != null) {
                @SuppressWarnings("unchecked")
                Set<String> users = (Set<String>) getServletContext().getAttribute("onlineUsers");
                if (users != null) {
                    users.remove(username);
                }
            }
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doPost(req, resp);
    }
}

