package edu.uestc.jspchatroom.web;

import edu.uestc.jspchatroom.dao.MessageDao;
import edu.uestc.jspchatroom.service.ChatService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/chat.jsp")
public final class ChatController extends PageController {
    private final ChatService chat = new ChatService();
    private int messageLimit = MessageDao.DEFAULT_LIMIT;

    @Override
    public void init() {
        String configured = getServletContext().getInitParameter("maxMessageCount");
        if (configured == null) return;
        try {
            int limit = Integer.parseInt(configured.trim());
            if (limit > 0) messageLimit = limit;
        } catch (NumberFormatException e) {
            log("Invalid maxMessageCount; using " + messageLimit);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        var user = currentUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        var snapshot = chat.snapshot(messageLimit, user.getId());
        request.setAttribute("currentUser", user);
        request.setAttribute("avatarVersions", snapshot.avatarVersions());
        request.setAttribute("messages", snapshot.messages());
        request.setAttribute("onlineUsers", snapshot.onlineUsers());
        request.setAttribute("onlineUserCount", snapshot.onlineUsers().size());
        request.setAttribute("totalMessages", snapshot.totalMessages());
        request.setAttribute("maxMessageCount", messageLimit);
        render(request, response, "fetch".equals(request.getHeader("X-Chat-Request")) ? "chat-updates" : "chat");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var user = currentUser(request);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chat.send(user.getId(), request.getParameter("msg"));
        if ("fetch".equals(request.getHeader("X-Chat-Request"))) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            redirect(request, response, "/chat.jsp?lang=" + language(request));
        }
    }
}
