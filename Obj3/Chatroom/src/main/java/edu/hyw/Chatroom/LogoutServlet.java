package edu.hyw.Chatroom;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * LogoutServlet 负责“退出登录”：
 * - POST 请求：从在线用户列表中移除当前用户，销毁 Session，并重定向回登录页面。
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取现有 Session
        HttpSession session = req.getSession(false);

        if (session != null) {
            // 从 Session 里拿到当前用户名
            String username = (String) session.getAttribute("user");

            if (username != null) {
                // 从 ServletContext 的全局在线列表移除该用户
                // TODO 9.
                ServletContext app = getServletContext();
                List<String> users = (List<String>) app.getAttribute("users");
                if (users != null ){
                    synchronized (users) {
                        users.remove(username);
                    }
                }
            }
            // 主动销毁 Session（清除所有会话级数据）
            // TODO 10.
            session.invalidate();
        }

        // 重定向回登录页（地址栏更新为 /login，也避免刷新重复提交）
        // TODO 11.
        resp.sendRedirect("login");
    }
}
