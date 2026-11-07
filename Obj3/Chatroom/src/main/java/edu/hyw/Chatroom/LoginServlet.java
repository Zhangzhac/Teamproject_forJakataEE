package edu.hyw.Chatroom;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * LoginServlet 负责用户登录。
 * - GET 请求：如果用户已登录，直接重定向到聊天室；否则显示登录表单。
 * - POST 请求：处理登录表单，创建 Session，并将用户加入在线用户列表。
 */

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 检查用户是否已经登录，已经登录 → 重定向到聊天室
        // TODO 1.
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect("chat");
            return;
        }
        // 未登录 → 显示登录表单
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>登录</title></head><body>");
        out.println("<h2>聊天室登录</h2>");
        out.println("<form method='post'>");
        out.println("用户名: <input type='text' name='username'/>");
        out.println("<input type='submit' value='登录'/>");
        out.println("</form>");
        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");

        if (username == null || username.isEmpty()) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().println("请输入用户名！");
            return;
        }

        // 保存用户名"user"到 Session
        // TODO 2.
        HttpSession session = req.getSession();
        session.setAttribute("user",username);
        // 更新全局在线用户列表"users"
        // TODO 3.
        ServletContext context = getServletContext();
        @SuppressWarnings("unchecked")
        List<String> users = (List<String>) context.getAttribute("users");
        if(users == null){
            users = new ArrayList<>();
            context.setAttribute("users",users);
        }
        if(!users.contains(username)){
            users.add(username);
        }
        // 登录成功 → 重定向到聊天室
        // TODO 4.
        resp.sendRedirect("chat");
    }
}
