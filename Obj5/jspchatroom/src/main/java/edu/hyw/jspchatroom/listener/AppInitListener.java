package edu.hyw.jspchatroom.listener;

import jakarta.servlet.*;
import java.util.*;

/**
 * AppInitListener
 * 应用启动时初始化全局共享数据（在线用户、消息列表、在线会话数）。
 */
public class AppInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext app = sce.getServletContext();
        app.setAttribute("users", Collections.synchronizedList(new ArrayList<String>()));
        app.setAttribute("msgs",  Collections.synchronizedList(new ArrayList<String>()));
        app.setAttribute("onlineCount", 0);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) { }
}


