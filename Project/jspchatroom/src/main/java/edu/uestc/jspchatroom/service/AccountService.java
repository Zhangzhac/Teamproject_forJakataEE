package edu.uestc.jspchatroom.service;

import edu.uestc.jspchatroom.dao.DAOException;
import edu.uestc.jspchatroom.dao.UserDao;
import edu.uestc.jspchatroom.model.User;
import edu.uestc.jspchatroom.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Date;

public final class AccountService {
    private final UserDao users = new UserDao();

    public record Login(User user, String error) {}

    public Login login(String username, String password, boolean databaseReady) {
        if (username.isEmpty()) return new Login(null, "error.username.empty");
        if (password == null || password.isEmpty()) return new Login(null, "error.password.empty");
        if (!databaseReady) return new Login(null, "error.db.notReady");

        User user = users.findByUsername(username);
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            return new Login(null, "error.login.failed");
        }
        users.markLogin(user.getId());
        user.setOnline(true);
        user.setLastLoginAt(new Date());
        user.setPassword(null);
        return new Login(user, null);
    }

    public String register(String username, String password, String confirmation, boolean databaseReady) {
        if (username.isEmpty()) return "error.username.empty";
        if (username.length() > 20) return "error.username.tooLong";
        if (!username.matches("[\\p{L}\\p{N}_]{2,20}")) return "error.username.invalid";
        if (password == null || password.isEmpty()) return "error.password.empty";
        if (password.length() < 6) return "error.password.tooShort";
        if (password.length() > 32) return "error.password.tooLong";
        if (!password.equals(confirmation)) return "error.password.mismatch";
        if (!databaseReady) return "error.db.notReady";
        try {
            users.create(username, PasswordUtil.hash(password));
            return null;
        } catch (DAOException e) {
            if (e.getCause() instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                return "error.user.exists";
            }
            throw e;
        }
    }

    public int registeredCount(boolean databaseReady) {
        return databaseReady ? users.countUsers() : 0;
    }
}
