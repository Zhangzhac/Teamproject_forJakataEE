package edu.uestc.jspchatroom.service;

import edu.uestc.jspchatroom.dao.MessageDao;
import edu.uestc.jspchatroom.dao.UserDao;
import edu.uestc.jspchatroom.model.Message;
import edu.uestc.jspchatroom.model.User;

import java.util.List;

public final class ChatService {
    private static final List<String> BLOCKED_WORDS = List.of("去死", "傻逼", "笨蛋");
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final MessageDao messages = new MessageDao();
    private final UserDao users = new UserDao();

    public record Snapshot(List<Message> messages, List<User> onlineUsers, int totalMessages) {}

    public Snapshot snapshot(int limit) {
        return new Snapshot(messages.findRecent(limit), users.findOnlineUsers(), messages.countMessages());
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
