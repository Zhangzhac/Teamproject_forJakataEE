package edu.uestc.jspchatroom.listener;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.List;

/**
 * SessionLifecycleListener
 * 监听会话创建/销毁；统计在线会话数，销毁时从在线用户列表移除。
 */
public class SessionLifecycleListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext app = se.getSession().getServletContext();
        synchronized (app) {
            Integer c = (Integer) app.getAttribute("onlineCount");
            if (c == null) c = 0;
            app.setAttribute("onlineCount", c + 1);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        ServletContext app = session.getServletContext();

        // 移除对应用户（处理超时/直接关闭浏览器等情况）
        String username = (String) session.getAttribute("user");
        if (username != null) {
            @SuppressWarnings("unchecked")
            List<String> users = (List<String>) app.getAttribute("users");
            if (users != null) users.remove(username);
        }

        synchronized (app) {
            Integer c = (Integer) app.getAttribute("onlineCount");
            if (c == null) c = 0;
            app.setAttribute("onlineCount", Math.max(0, c - 1));
        }
    }
}