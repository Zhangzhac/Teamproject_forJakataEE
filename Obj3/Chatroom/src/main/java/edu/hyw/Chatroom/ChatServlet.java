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
 * ChatServlet 负责聊天室页面与发消息逻辑。
 * - GET 请求：检查是否已登录，未登录则重定向到登录页；已登录则显示聊天室页面（在线用户和消息记录）。
 * - POST 请求：处理用户发送的消息，保存到全局消息列表并控制条数上限，然后重定向回聊天室。
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    /** 消息数量上限的默认值（当未配置或配置非法时使用） */
    private int maxMessageCount = 50;

    @Override
    public void init() {
        // 从 web.xml 读取全局参数 maxMessageCount
        // TODO 5.
        String param = getInitParameter("maxMessageCount");
        if (param != null && !param.isEmpty()) {
            try {
                maxMessageCount = Integer.parseInt(param.trim());
            } catch (NumberFormatException e) {
            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");

        // 校验登录状态，如果未登录，则重定向到login
        // TODO 6.
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        // 取出用户名
        String user = (String) session.getAttribute("user");

        // 取出全局的在线用户与消息列表
        ServletContext app = getServletContext();
        List<String> users = (List<String>) app.getAttribute("users");
        List<String> msgs  = (List<String>) app.getAttribute("msgs");

        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>聊天室</title></head><body>");
        out.println("<h2>欢迎你, " + user + "！</h2>");

        out.println("<h3>当前在线用户:</h3>");
        if (users != null) {
            for (String u : users) {
                out.println(u + "<br>");
            }
        }

        out.println("<h3>聊天记录（最多显示 " + maxMessageCount + " 条）:</h3>");
        if (msgs != null) {
            for (String m : msgs) {
                out.println(m + "<br>");
            }
        }

        out.println("<form method='post'>");
        out.println("消息: <input type='text' name='msg'/>");
        out.println("<input type='submit' value='发送'/>");
        out.println("</form>");

        out.println("<form method='post' action='logout'>");
        out.println("<input type='submit' value='退出登录'/>");
        out.println("</form>");

        out.println("</body></html>");
        resp.setHeader("Refresh", "15; url=chat");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect("login");
            return;
        }

        String user = (String) session.getAttribute("user");
        String msg  = req.getParameter("msg");

        if (msg != null && !msg.isEmpty()) {
            ServletContext app = getServletContext();
            List<String> msgs = (List<String>) app.getAttribute("msgs");
            if (msgs == null) {
                msgs = new ArrayList<>();
                app.setAttribute("msgs", msgs);
            }

            // 保存消息到消息列表msg，注意消息上限，注意线程安全
            // TODO 7.
            synchronized (msgs) {
                msgs.add(user + ":" + msg);
                while (msgs.size() > maxMessageCount) {
                    msgs.remove(0);
                }
                }
        }

        // 重定向回当前页面
        // TODO 8.
        resp.sendRedirect("chat");
    }
}
