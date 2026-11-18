package edu.uestc.jspchatroom.web;

import edu.uestc.jspchatroom.dao.DAOException;
import edu.uestc.jspchatroom.model.User;
import edu.uestc.jspchatroom.service.AvatarService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@WebServlet("/settings.jsp")
@MultipartConfig(fileSizeThreshold = 64 * 1024,
        maxFileSize = AvatarService.MAX_UPLOAD_BYTES,
        maxRequestSize = AvatarService.MAX_UPLOAD_BYTES + 64 * 1024)
public final class SettingsController extends PageController {
    private static final String TOKEN = "avatarUploadToken";

    private final AvatarService avatars = new AvatarService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("avatarSaved", "1".equals(request.getParameter("avatarSaved")));
        show(request, response, currentUser(request));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Part avatar = null;
        try {
            avatar = request.getPart("avatar");
            String expected = (String) request.getSession(false).getAttribute(TOKEN);
            if (expected == null || !expected.equals(request.getParameter(TOKEN))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (avatar == null || avatar.getSize() == 0 || avatar.getSubmittedFileName() == null) {
                showError(request, response, user, 400, "avatar.choose");
                return;
            }
            try (var stream = avatar.getInputStream()) {
                avatars.save(user.getId(), stream);
            }
            redirect(request, response, "/settings.jsp?lang=" + language(request) + "&avatarSaved=1");
        } catch (IllegalStateException e) {
            showError(request, response, user, 413, "avatar.tooLarge");
        } catch (AvatarService.InvalidAvatar e) {
            showError(request, response, user, 400, e.getMessage());
        } catch (ServletException e) {
            showError(request, response, user, 400, "avatar.invalid");
        } catch (DAOException | IOException e) {
            log("Avatar upload failed", e);
            showError(request, response, user, 500, "avatar.failed");
        } finally {
            if (avatar != null) {
                try { avatar.delete(); }
                catch (IOException e) { log("Could not remove temporary avatar upload", e); }
            }
        }
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, User user,
                           int status, String message) throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("avatarError", message);
        show(request, response, user);
    }

    private void show(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if (user == null) {
            redirect(request, response, "/login.jsp?lang=" + language(request));
            return;
        }
        var session = request.getSession(false);
        String token;
        synchronized (session) {
            token = (String) session.getAttribute(TOKEN);
            if (token == null) {
                token = UUID.randomUUID().toString();
                session.setAttribute(TOKEN, token);
            }
        }
        request.setAttribute("currentUser", user);
        request.setAttribute(TOKEN, token);
        request.setAttribute("avatarVersions", avatars.versions(Set.of(user.getId())));
        render(request, response, "settings");
    }
}
