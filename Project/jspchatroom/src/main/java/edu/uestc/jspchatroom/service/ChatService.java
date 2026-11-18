package edu.uestc.jspchatroom.service;

import edu.uestc.jspchatroom.dao.MessageDao;
import edu.uestc.jspchatroom.dao.UserDao;
import edu.uestc.jspchatroom.model.Message;
import edu.uestc.jspchatroom.model.User;

import java.util.List;
import java.util.HashSet;
import java.util.Map;

public final class ChatService {
    private static final List<String> BLOCKED_WORDS = List.of("去死", "傻逼", "笨蛋");
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final MessageDao messages = new MessageDao();
    private final UserDao users = new UserDao();
    private final AvatarService avatars = new AvatarService();

    public record Snapshot(List<Message> messages, List<User> onlineUsers, int totalMessages,
                           Map<Integer, String> avatarVersions) {}

    public Snapshot snapshot(int limit, int currentUserId) {
        List<Message> recent = messages.findRecent(limit);
        List<User> online = users.findOnlineUsers();
        var avatarUsers = new HashSet<Integer>();
        avatarUsers.add(currentUserId);
        recent.forEach(message -> avatarUsers.add(message.getUserId()));
        online.forEach(user -> avatarUsers.add(user.getId()));
        return new Snapshot(recent, online, messages.countMessages(), avatars.versions(avatarUsers));
    }

    public void send(int userId, String text) {
        if (text == null) return;
        for (String word : BLOCKED_WORDS) text = text.replace(word, "**");
        text = text.trim();
        if (text.length() > MAX_MESSAGE_LENGTH) {
            int end = MAX_MESSAGE_LENGTH;
            if (Character.isHighSurrogate(text.charAt(end - 1))) end--;
            text = text.substring(0, end);
        }
        if (!text.isEmpty()) messages.insert(userId, text);
    }
}
