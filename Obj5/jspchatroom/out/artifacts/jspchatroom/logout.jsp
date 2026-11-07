<%--
  Created by IntelliJ IDEA.
  User: zhang
  Date: 2026/10/16
  Time: 16:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<%
    // ============ 1. 从全局在线会话表中移除当前会话 ============
    // chat.jsp 把在线会话存在 application 的 "onlineSessions" 属性里，
    // 退出时要同步清掉，否则"在线用户"里还会残留当前用户。
    Map<String, Object[]> onlineSessions =
            (Map<String, Object[]>) application.getAttribute("onlineSessions");
    if (onlineSessions != null) {
        // 这里不能用 session.getId()，因为下面马上要 invalidate；
        // 但当前会话还没失效，所以此时 getId() 仍可安全调用。
        onlineSessions.remove(session.getId());
    }
    // ============ 2. 让 session 失效 ============
    // 可选：记录退出用户名（仅用于展示时使用，失效前取）
    String byeUser = (String) session.getAttribute("username");
    try {
        session.invalidate();
    } catch (IllegalStateException ignore) {
        // 会话已经失效，忽略
    }
    // ============ 3. 重定向回登录页 ============
    response.sendRedirect(request.getContextPath() + "/login.jsp");
    return; // 不再输出本页内容
%>
