<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.hyw.jspchatroom.dao.UserDao" %>
<%@ page import="edu.hyw.jspchatroom.model.User" %>
<%--
    退出登录（实验 07）
    ---------------------------------------------------------------
    1. 把数据库 users.online 置为 FALSE（“在线用户”里立刻消失）；
    2. 让当前 HttpSession 失效；
    3. 跳回登录页。
    说明：如果用户是直接关闭浏览器、会话超时后才失效的，
    则由 SessionLifecycleListener#sessionDestroyed 负责把 online 改回 FALSE。
--%>
<%
    request.setCharacterEncoding("UTF-8");

    Object current = session.getAttribute("currentUser");
    if (current instanceof User user && user.getId() > 0) {
        try {
            new UserDao().setOnline(user.getId(), false);
            System.out.println("[logout] 用户已退出: " + user.getUsername());
        } catch (RuntimeException e) {
            System.err.println("[logout] 更新在线状态失败: " + e.getMessage());
        }
    }

    // 让会话失效（会触发 SessionLifecycleListener#sessionDestroyed）
    try {
        session.invalidate();
    } catch (IllegalStateException ignore) {
        // 会话已经失效，忽略
    }

    String lang = request.getParameter("lang");
    String langQuery = ("zh".equals(lang) || "en".equals(lang)) ? "?lang=" + lang : "";
    response.sendRedirect(request.getContextPath() + "/login.jsp" + langQuery);
%>
