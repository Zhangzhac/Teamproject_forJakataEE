package edu.uestc.jspchatroom.web;

import edu.uestc.jspchatroom.dao.DAOException;
import edu.uestc.jspchatroom.service.TranslationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/translate")
public final class TranslateServlet extends PageController {
    private final TranslationService translations = new TranslationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean english = "en".equals(request.getParameter("ui"));
        request.setAttribute("pageLang", english ? "en" : "zh");
        response.setHeader("Cache-Control", "no-store");

        if (currentUser(request) == null) {
            error(request, response, 401, "translate.signIn");
            return;
        }

        String target = request.getParameter("lang");
        String id = request.getParameter("messageId");
        if (!TranslationService.supports(target) || id == null || id.isBlank()) {
            error(request, response, 400, "translate.parameters");
            return;
        }

        int messageId;
        try {
            messageId = Integer.parseInt(id.trim());
            if (messageId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            error(request, response, 400, "translate.messageId");
            return;
        }

        String text;
        try {
            text = translations.translate(messageId, target, request.getParameter("source"));
        } catch (IllegalArgumentException e) {
            String[] localized = e.getMessage().split(" / ", 2);
            request.setAttribute("translationError", localized[english && localized.length == 2 ? 1 : 0]);
            response.setStatus(400);
            showResult(request, response);
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            error(request, response, 502, "translate.unavailable");
            return;
        } catch (IOException | DAOException e) {
            log("Could not translate message " + messageId, e);
            error(request, response, 502, "translate.unavailable");
            return;
        }

        if (text == null) {
            error(request, response, 404, "translate.messageMissing");
            return;
        }
        request.setAttribute("translationLanguage", "translate.language." + target);
        request.setAttribute("translationText", text);
        showResult(request, response);
    }

    private void error(HttpServletRequest request, HttpServletResponse response, int status, String key)
            throws ServletException, IOException {
        response.setStatus(status);
        request.setAttribute("translationErrorKey", key);
        showResult(request, response);
    }

    private void showResult(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/translation.jsp").forward(request, response);
    }
}
