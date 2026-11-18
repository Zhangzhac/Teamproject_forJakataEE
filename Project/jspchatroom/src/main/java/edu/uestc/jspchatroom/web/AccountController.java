package edu.uestc.jspchatroom.web;

import edu.uestc.jspchatroom.service.AccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet({"/login.jsp", "/register.jsp", "/logout.jsp"})
public final class AccountController extends PageController {
    private final AccountService accounts = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/register.jsp" -> render(request, response, "register");
            case "/logout.jsp" -> logout(request, response);
            default -> showLogin(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/logout.jsp".equals(request.getServletPath())) {
            logout(request, response);
            return;
        }

        String username = request.getParameter("username");
        username = username == null ? "" : username.trim();
        String password = request.getParameter("password");

        if ("/register.jsp".equals(request.getServletPath())) {
            String error = accounts.register(username, password, request.getParameter("confirm"), databaseReady());
            if (error == null) {
                redirect(request, response, "/login.jsp?registered=1&lang=" + language(request));
            } else {
                request.setAttribute("error", error);
                render(request, response, "register");
            }
            return;
        }

        var login = accounts.login(username, password, databaseReady());
        if (login.error() != null) {
            request.setAttribute("error", login.error());
            showLogin(request, response);
            return;
        }

        var session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute("currentUser", login.user());
        session.setAttribute("username", login.user().getUsername());
        session.setMaxInactiveInterval(30 * 60);
        redirect(request, response, "/chat.jsp?lang=" + language(request));
    }

    private void showLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("registeredUsers", accounts.registeredCount(databaseReady()));
        render(request, response, "login");
    }

    private boolean databaseReady() {
        return Boolean.TRUE.equals(getServletContext().getAttribute("dbReady"));
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        redirect(request, response, "/login.jsp?lang=" + language(request));
    }
}
