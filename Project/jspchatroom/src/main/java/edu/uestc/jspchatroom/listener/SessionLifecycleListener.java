package edu.uestc.jspchatroom.listener;

import edu.uestc.jspchatroom.dao.UserDao;
import edu.uestc.jspchatroom.model.User;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class SessionLifecycleListener implements HttpSessionListener {
    private static final Logger LOGGER = Logger.getLogger(SessionLifecycleListener.class.getName());

    private final UserDao users = new UserDao();

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        Object current = event.getSession().getAttribute("currentUser");
        if (current instanceof User user && user.getId() > 0) {
            try {
                users.setOnline(user.getId(), false);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Could not mark user offline: " + user.getId(), e);
            }
        }
    }
}
