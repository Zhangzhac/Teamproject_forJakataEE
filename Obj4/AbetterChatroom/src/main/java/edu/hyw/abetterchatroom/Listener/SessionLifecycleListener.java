package edu.hyw.abetterchatroom.Listener;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.*;

import java.util.Set;

/**
 * SessionLifecycleListener
 * 监听会话创建/销毁；统计在线会话数，销毁时从在线用户列表移除。
 */
public class SessionLifecycleListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // TODO
        HttpSession session = se.getSession();
        ServletContext context = session.getServletContext();

        synchronized (context){
            Integer count = (Integer)context.getAttribute("onlineCount");
            if(count ==null){
                count =0;
            }
            count++;
            context.setAttribute("onlineCount", count);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        // TODO
        HttpSession session = se.getSession();
        ServletContext context = session.getServletContext();
        synchronized (context){
            Integer count = (Integer)context.getAttribute("onlineCount");
            if(count != null && count >0){
                count--;
                context.setAttribute("onlineCount", count);
            }
        }
        Set<String> onlineUsers = (Set<String>)context.getAttribute("onlineUsers");
        if(onlineUsers != null){
            Object username = session.getAttribute("username");
            if(username != null){
                onlineUsers.remove(username.toString());
            }
        }
    }
}
