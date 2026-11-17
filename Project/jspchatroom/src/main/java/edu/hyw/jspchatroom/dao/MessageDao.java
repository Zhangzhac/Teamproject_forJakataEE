package edu.hyw.jspchatroom.dao;

import edu.hyw.jspchatroom.model.Message;
import edu.hyw.jspchatroom.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MessageDao —— messages 表的数据访问对象（DAO）。
 *
 * <p>核心 SQL 两条：</p>
 * <pre>
 *   插入： INSERT INTO messages (user_id, content, created_at) VALUES (?, ?, ?)
 *   查询： SELECT m.*, u.username FROM messages m
 *          JOIN users u ON u.id = m.user_id
 *          ORDER BY m.created_at DESC, m.id DESC LIMIT ?
 * </pre>
 *
 * <p>查询时按时间<b>倒序</b>取最近 N 条（对应实验要求“读取最近若干条聊天记录”，
 * 也是思考题 3 关于分页/限制条数的做法），再在 Java 里反转成“由旧到新”，
 * 这样页面上就是符合聊天习惯的顺序。</p>
 */
public class MessageDao {

    /** 默认最多读取的消息条数。 */
    public static final int DEFAULT_LIMIT = 50;

    /**
     * 插入一条聊天消息。
     *
     * @param userId  发送者编号（外键 → users.id）
     * @param content 消息内容
     * @return 新消息的主键
     */
    public int insert(int userId, String content) {
        String sql = "INSERT INTO messages (user_id, content, created_at) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, content);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DAOException("保存聊天消息失败", e);
        }
    }

    /**
     * 读取最近 {@code limit} 条聊天记录（含发送者用户名），按时间正序返回。
     *
     * @param limit 最多返回多少条；小于等于 0 时使用 {@link #DEFAULT_LIMIT}
     */
    public List<Message> findRecent(int limit) {
        int n = (limit <= 0) ? DEFAULT_LIMIT : limit;
        String sql = "SELECT m.id, m.user_id, m.content, m.created_at, u.username "
                + "FROM messages m "
                + "JOIN users u ON u.id = m.user_id "
                + "ORDER BY m.created_at DESC, m.id DESC "
                + "LIMIT ?";
        List<Message> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("id"));
                    m.setUserId(rs.getInt("user_id"));
                    m.setUsername(rs.getString("username"));
                    m.setContent(rs.getString("content"));
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("读取聊天记录失败", e);
        }
        // DESC 取到的“最近 N 条”反转成由旧到新，页面显示更自然
        Collections.reverse(list);
        return list;
    }

    /** 全部消息条数（可在页面上展示“共 N 条历史消息”）。 */
    public int countMessages() {
        String sql = "SELECT COUNT(*) FROM messages";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DAOException("统计消息数失败", e);
        }
    }
}
