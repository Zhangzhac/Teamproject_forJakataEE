package edu.uestc.jspchatroom.web;

import edu.uestc.jspchatroom.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

abstract class PageController extends HttpServlet {
    protected static String language(HttpServletRequest request) {
        String explicit = request.getParameter("lang");
        if ("en".equals(explicit) || "zh".equals(explicit)) return explicit;
        return "en".equals(request.getLocale().getLanguage()) ? "en" : "zh";
    }

    protected static User currentUser(HttpServletRequest request) {
        var session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("currentUser");
        return value instanceof User user && user.getId() > 0 ? user : null;
    }

    protected static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected static void render(HttpServletRequest request, HttpServletResponse response, String page)
            throws ServletException, IOException {
        request.setAttribute("pageLang", language(request));
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/WEB-INF/views/" + page + ".jsp").forward(request, response);
    }
}
