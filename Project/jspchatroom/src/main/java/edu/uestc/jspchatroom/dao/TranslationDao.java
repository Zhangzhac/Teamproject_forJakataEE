package edu.uestc.jspchatroom.dao;

import edu.uestc.jspchatroom.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TranslationDao —— 翻译缓存表 message_translation 的数据访问对象。
 *
 * <pre>
 *   message_translation
 *   ├─ message_id  消息编号（联合主键，外键 → messages.id）
 *   ├─ lang        目标语言代码，如 zh / en / ja
 *   ├─ content     译文
 *   └─ created_at  翻译时间
 * </pre>
 *
 * <p>为什么要有这张表：免费翻译接口有每日额度，同一条消息被 5 个人点、
 * 或者同一个人反复点，只要缓存过一次就只调用 1 次接口。</p>
 *
 * <p>注意这里额外提供了 {@link #findMessageContent(int)}：
 * 取原文本来可以放 {@code MessageDao}，但那个类组员正在做实时更新（要加 findSince），
 * 翻译功能尽量不去碰它，减少两边的改动撞车。</p>
 */
public class TranslationDao {

    /**
     * 读缓存。
     *
     * @return 已翻译过的文本；没翻译过返回 {@code null}
     */
    public String findTranslation(int messageId, String lang) {
        String sql = "SELECT content FROM message_translation WHERE message_id = ? AND lang = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setString(2, lang);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("读取翻译缓存失败", e);
        }
    }

    /**
     * 写缓存（同一条消息 + 同一种语言只保留一份）。
     *
     * <p>用 H2 的 {@code MERGE ... KEY(...)}：主键已存在时更新，不存在时插入，
     * 所以重复翻译同一条消息不会报主键冲突。</p>
     */
    public void saveTranslation(int messageId, String lang, String text) {
        String sql = "MERGE INTO message_translation (message_id, lang, content) "
                + "KEY (message_id, lang) VALUES (?, ?, ?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ps.setString(2, lang);
            ps.setString(3, text);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("保存翻译结果失败", e);
        }
    }

    /**
     * 取某条消息的原文。
     *
     * @return 原文；消息不存在返回 {@code null}
     */
    public String findMessageContent(int messageId) {
        String sql = "SELECT content FROM messages WHERE id = ?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("读取消息原文失败", e);
        }
    }
}
