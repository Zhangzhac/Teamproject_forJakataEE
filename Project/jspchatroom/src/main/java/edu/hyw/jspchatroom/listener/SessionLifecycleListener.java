package edu.hyw.jspchatroom.listener;

import edu.hyw.jspchatroom.dao.UserDao;
import edu.hyw.jspchatroom.model.User;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * SessionLifecycleListener
 * 监听会话创建/销毁：统计在线会话数，并在会话结束时把数据库中的在线状态改回 FALSE。
 *
 * <p>实验 07 的 users 表有“在线状态”字段，因此退出登录、会话超时、
 * 直接关闭浏览器（会话超时后触发）这几种情况都要同步数据库，
 * 否则“在线用户”列表会出现幽灵用户。</p>
 */
public class SessionLifecycleListener implements HttpSessionListener {

    private final UserDao userDao = new UserDao();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext app = se.getSession().getServletContext();
        synchronized (app) {
            Object c = app.getAttribute("onlineCount");
            int count = (c instanceof Integer i) ? i : 0;
            app.setAttribute("onlineCount", count + 1);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        ServletContext app = session.getServletContext();

        // 1. 把该用户的在线状态写回数据库（退出登录 / 会话超时都会走到这里）
        Object current = session.getAttribute("currentUser");
        if (current instanceof User user && user.getId() > 0) {
            try {
                userDao.setOnline(user.getId(), false);
                System.out.println("[SessionLifecycleListener] 用户下线: " + user.getUsername());
            } catch (RuntimeException e) {
                // 应用停止过程中数据库可能已关闭，这里不能让监听器抛异常
                System.err.println("[SessionLifecycleListener] 更新在线状态失败: " + e.getMessage());
            }
        }

        // 2. 在线会话计数减一
        synchronized (app) {
            Object c = app.getAttribute("onlineCount");
            int count = (c instanceof Integer i) ? i : 0;
            app.setAttribute("onlineCount", Math.max(0, count - 1));
        }
    }
}
