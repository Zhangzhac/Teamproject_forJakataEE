package edu.uestc.jspchatroom.web;

import edu.uestc.jspchatroom.service.AvatarService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/avatar")
public final class AvatarServlet extends PageController {
    private final AvatarService avatars = new AvatarService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "private, no-cache");
        if (currentUser(request) == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(request.getParameter("id"));
            if (userId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var avatar = avatars.find(userId);
        if (avatar == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String etag = "\"" + avatar.version() + "\"";
        response.setHeader("ETag", etag);
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (etag.equals(request.getHeader("If-None-Match"))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setCharacterEncoding(null);
        response.setContentType("image/jpeg");
        response.setContentLength(avatar.jpeg().length);
        response.getOutputStream().write(avatar.jpeg());
    }
}
