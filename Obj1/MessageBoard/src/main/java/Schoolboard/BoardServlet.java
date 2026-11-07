package Schoolboard;


import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@WebServlet("/board")
    public class BoardServlet extends HttpServlet {
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("""
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><title>Mini Board</title></head>
                <body>
                <h2>Mini Message Board</h2>
                <form method="post" action="board">
                    Nickname: <input name="nickname" maxlength="20" required>
                    <br>Message: <input name="content" maxlength="140" required style="width:320px">
                    <br><button type="submit">Post</button>
                </form><hr>
                <h3>Messages</h3>
                """);
        if (messages.isEmpty()) {
            out.println("<p><i>(No messages yet)</i></p>");
        } else {
            for (Message m : messages) {
                out.println("<p><strong>" + esc(m.getNickname()) + ":</strong> " + esc(m.getContent()) + "</p>");
            }
        }
        out.println("</body></html>");
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        String nickname = req.getParameter("nickname");
        String content = req.getParameter("content");
        if (nickname != null) nickname = nickname.trim();
        if (content != null) content = content.trim();
        if (nickname != null && !nickname.isEmpty() && nickname.length() <= 20
                && content != null && !content.isEmpty() && content.length() <= 140) {
            messages.add(0, new Message(nickname, content));
        }
        resp.sendRedirect("board");
    }
    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}