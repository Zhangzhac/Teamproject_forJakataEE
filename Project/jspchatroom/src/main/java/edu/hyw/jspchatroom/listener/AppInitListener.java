package edu.hyw.jspchatroom.listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * AppInitListener
 * 应用启动时初始化全局共享数据（在线会话计数）。
 *
 * <p>注意：第 06 次实验把消息和用户存在 application 内存里；
 * 实验 07 起，消息与用户都改为持久化到 H2 数据库（见 DBInitListener、
 * MessageDao、UserDao），因此这里只保留与“会话计数”有关的初始化。</p>
 */
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext app = sce.getServletContext();
        app.setAttribute("onlineCount", 0);
        System.out.println("[AppInitListener] 应用已启动，在线会话计数初始化完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
