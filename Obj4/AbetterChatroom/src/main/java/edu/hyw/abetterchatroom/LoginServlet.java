package edu.hyw.abetterchatroom;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoginServlet
 * 展示登录页并处理登录：将用户名写入 Session，并加入全局在线用户列表。
 */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("""
              <!doctype html><html><head><meta charset="UTF-8"><title>登录</title></head><body>
              <h2>登录聊天室</h2>
              <form method="post" action="login">
                用户名：<input name="username" required>
                <button type="submit">进入</button>
              </form>
              </body></html>
            """);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String username = req.getParameter("username");
        if (username == null || username.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        username = username.trim();

        HttpSession session = req.getSession(true);
        session.setAttribute("user", username);

        ServletContext app = getServletContext();
        @SuppressWarnings("unchecked")
        Set<String > users = (Set<String>) app.getAttribute("onlineUsers");
        if (users == null) {
            users = ConcurrentHashMap.newKeySet();
            app.setAttribute("onlineUsers", users);
        }
        users.add(username);
        resp.sendRedirect(req.getContextPath() + "/chat");
    }
}
