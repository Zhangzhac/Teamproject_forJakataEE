package edu.hyw.abetterchatroom.Listener;

import jakarta.servlet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javax.tools.JavaCompiler.*;

/**
 * AppInitListener
 * 应用启动时初始化全局共享数据（在线用户、消息列表、在线会话数）。
 */
public class AppInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // TODO
        ServletContext context = sce.getServletContext();
        // 在线用户
        Set<String> OnlineUsers = ConcurrentHashMap.newKeySet();
        context.setAttribute("OnlineUsers", OnlineUsers);
        // 消息列表
        List<String> messages = Collections.synchronizedList(new ArrayList<>());
        context.setAttribute("messages", messages);
        //在线会话数，初始为0
        context.setAttribute("onlineCount", 0);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 清理资源（如有需要）
    }
}

