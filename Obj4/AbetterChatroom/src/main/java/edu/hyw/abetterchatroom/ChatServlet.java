package edu.hyw.abetterchatroom;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChatServlet
 * 展示聊天室页面并处理消息发送；消息上限由 context-param 控制。
 */
public class ChatServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    private List<String> getMsgs() {
        ServletContext context = getServletContext();
        List<String> msgs = (List<String>) context.getAttribute("msgs");
        if(msgs == null){
            msgs = Collections.synchronizedList(new ArrayList<>());
            context.setAttribute("msgs", msgs);
        }
        return msgs;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getUsers() {
        ServletContext context = getServletContext();
        Set<String> users = (Set<String>) context.getAttribute("onlineUsers");
        if(users == null){
            users = ConcurrentHashMap.newKeySet();
            context.setAttribute("onlineUsers", users);
        }
        return users;
    }

    private int getMaxMsgCount() {
        String v = getServletContext().getInitParameter("maxMessageCount");
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 50;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        HttpSession session = req.getSession(false);
        String user = (session == null) ? "" : (String) session.getAttribute("user");
        long lastActive = (session == null || session.getAttribute("lastActiveAt") == null)
                ? 0L : (long) session.getAttribute("lastActiveAt");

        Set<String> users = getUsers();
        List<String> msgs = getMsgs();

        Object count = getServletContext().getAttribute("onlineCount");
        int onlineCount = (count instanceof Integer) ? (Integer) count : 0;
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!doctype html><html><head><meta charset='UTF-8'><title>聊天室</title></head><body>");

            out.printf("<p>当前用户：%s | 在线会话数：%d | 最近活跃：%s</p>",
                    user, onlineCount,
                    (lastActive == 0 ? "-" : new Date(lastActive).toString()));

            out.println("<h3>消息列表</h3><ul>");
            synchronized (msgs) {
                for (String m : msgs) {
                    out.printf("<li>%s</li>%n", m);
                }
            }
            out.println("</ul>");

            out.println("<form method='post' action='chat'>");
            out.println("<textarea name='msg' rows='3'></textarea><br/>");
            out.println("<button type='submit'>发送</button>");
            out.println("</form>");

            out.println("<h3>在线用户</h3><ul>");
            synchronized (users) {
                for (String u : users) {
                    out.printf("<li>%s</li>%n", u);
                }
            }
            out.println("</ul>");

            out.println("<form method='post' action='logout'><button>退出登录</button></form>");

            out.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String user = (String) session.getAttribute("user");

        // 优先使用 EncodingFilter 过滤后的文本
        String filtered = (String) req.getAttribute("filteredMsg");
        String raw = req.getParameter("msg");
        String msg = filtered != null ? filtered : (raw == null ? "" : raw);

        if (!msg.isEmpty()) {
            List<String> msgs = getMsgs();
            int max = getMaxMsgCount();
            synchronized (msgs) {
                msgs.add("<b>" + user + "</b>: " + msg);
                while (msgs.size() > max) msgs.remove(0);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/chat");
    }
}
