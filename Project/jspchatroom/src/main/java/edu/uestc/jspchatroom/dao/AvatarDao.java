package edu.uestc.jspchatroom.dao;

import edu.uestc.jspchatroom.util.DBUtil;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AvatarDao {
    public record Avatar(String version, byte[] jpeg) {}

    public Avatar find(int userId) {
        String sql = "SELECT revision, jpeg FROM user_avatars WHERE user_id = ?";
        try (var connection = DBUtil.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (var rows = statement.executeQuery()) {
                return rows.next() ? new Avatar(rows.getString(1), rows.getBytes(2)) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("Could not load avatar", e);
        }
    }

    public Map<Integer, String> findVersions(Set<Integer> userIds) {
        if (userIds.isEmpty()) return Map.of();
        String placeholders = String.join(",", Collections.nCopies(userIds.size(), "?"));
        String sql = "SELECT user_id, revision FROM user_avatars WHERE user_id IN (" + placeholders + ")";
        try (var connection = DBUtil.getConnection(); var statement = connection.prepareStatement(sql)) {
            int parameter = 1;
            for (int userId : userIds) statement.setInt(parameter++, userId);
            try (var rows = statement.executeQuery()) {
                Map<Integer, String> revisions = new HashMap<>();
                while (rows.next()) revisions.put(rows.getInt(1), rows.getString(2));
                return revisions;
            }
        } catch (SQLException e) {
            throw new DAOException("Could not load avatar versions", e);
        }
    }

    public void save(int userId, String version, byte[] jpeg) {
        String sql = "MERGE INTO user_avatars (user_id, revision, jpeg) KEY (user_id) VALUES (?, ?, ?)";
        try (var connection = DBUtil.getConnection(); var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, version);
            statement.setBytes(3, jpeg);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("Could not save avatar", e);
        }
    }
}
