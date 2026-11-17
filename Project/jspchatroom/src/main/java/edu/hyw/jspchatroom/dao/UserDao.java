package edu.hyw.jspchatroom.dao;

import edu.hyw.jspchatroom.model.User;
import edu.hyw.jspchatroom.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDao —— users 表的数据访问对象（DAO）。
 *
 * <p>把“注册、登录校验、在线状态”相关的 SQL 全部集中在这里，
 * JSP 页面只调用方法、不写 SQL —— 这是向 MVC 过渡的第一步：
 * <b>视图只负责显示，数据访问只负责 SQL</b>。</p>
 *
 * <p>所有 SQL 都使用 {@link PreparedStatement} 参数占位符 {@code ?}，
 * 从根本上避免 SQL 注入。</p>
 */
public class UserDao {

    /**
     * 按用户名查询用户（登录校验、注册查重用）。
     *
     * @return 找到返回 User，找不到返回 null
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, created_at, last_login_at, online "
                + "FROM users WHERE username = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("查询用户失败: " + username, e);
        }
    }

    /** 用户名是否已被注册。 */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("检查用户名是否存在失败: " + username, e);
        }
    }

    /**
     * 注册新用户：向 users 表插入一行。
     *
     * @param username      用户名
     * @param passwordHash  已经加盐摘要后的密码串（见 PasswordUtil）
     * @return 插入后的完整用户对象（含自增主键）
     */
    public User create(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password, created_at, last_login_at, online) "
                + "VALUES (?, ?, ?, NULL, FALSE)";
        Timestamp now = new Timestamp(System.currentTimeMillis());
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setTimestamp(3, now);
            ps.executeUpdate();
            int id = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
            User user = new User();
            user.setId(id);
            user.setUsername(username);
            user.setPassword(passwordHash);
            user.setCreatedAt(now);
            user.setOnline(false);
            return user;
        } catch (SQLException e) {
            throw new DAOException("注册用户失败: " + username, e);
        }
    }

    /**
     * 登录成功后：更新“最近登录时间”并把在线状态置为 TRUE。
     */
    public void markLogin(int userId) {
        String sql = "UPDATE users SET last_login_at = ?, online = TRUE WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("更新登录时间失败: id=" + userId, e);
        }
    }

    /** 更新在线状态（退出登录 / 会话超时时置为 FALSE）。 */
    public void setOnline(int userId, boolean online) {
        String sql = "UPDATE users SET online = ? WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, online);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("更新在线状态失败: id=" + userId, e);
        }
    }

    /** 查询当前在线用户列表（chat.jsp 右侧“在线用户”来自这里）。 */
    public List<User> findOnlineUsers() {
        String sql = "SELECT id, username, password, created_at, last_login_at, online "
                + "FROM users WHERE online = TRUE ORDER BY last_login_at DESC";
        List<User> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DAOException("查询在线用户失败", e);
        }
    }

    /**
     * 应用启动时把所有用户的在线状态清零。
     *
     * <p>Tomcat 重启后内存中的会话全部消失，库里残留的 online=TRUE 就成了脏数据，
     * 所以 DBInitListener 会在启动阶段调用本方法，保证“在线用户”显示正确。</p>
     *
     * @return 被重置的行数
     */
    public int resetAllOnline() {
        String sql = "UPDATE users SET online = FALSE WHERE online = TRUE";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("重置在线状态失败", e);
        }
    }

    /** 已注册用户总数（登录页可以显示，作为“数据持久化生效”的直观证据）。 */
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DAOException("统计用户数失败", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setLastLoginAt(rs.getTimestamp("last_login_at"));
        u.setOnline(rs.getBoolean("online"));
        return u;
    }
}
