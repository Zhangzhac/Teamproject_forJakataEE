<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ page import="java.util.List" %>
<%
    // 处理 POST（提交登录）
    // todo
    request.setCharacterEncoding("UTF-8");   // 防止中文用户名乱码
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String username = request.getParameter("username");
        if (username != null) {
            username = username.trim();
        }
        if (username == null || username.isEmpty()) {
            request.setAttribute("error", "用户名不能为空");
        } else if (username.length() > 20) {
            request.setAttribute("error", "用户名长度不能超过 20 个字符");
        } else {
            // 注意：本页 session="false"，没有隐式 session 对象，
            // 必须通过 request.getSession(true) 显式创建
            HttpSession chatSession = request.getSession(true);
            chatSession.setAttribute("username", username);
            chatSession.setMaxInactiveInterval(30 * 60); // 30 分钟不活动则失效
            response.sendRedirect(request.getContextPath() + "/chat.jsp");
            return;   // 重定向后必须 return，不再输出下面页面
        }
    }
    String error = (String) request.getAttribute("error");
%>
<!doctype html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <title>登录聊天室</title>
</head>
<body>
<h2>登录聊天室（JSP 版）</h2>
<form method="post" action="login.jsp">
    用户名：<input name="username" required>
    <button type="submit">进入</button>
</form>
</body>
</html>
