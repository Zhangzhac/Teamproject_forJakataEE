<%--
  Created by IntelliJ IDEA.
  User: zhang
  Date: 2026/10/16
  Time: 16:40
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // ============ 1. 从全局在线会话表中移除当前会话 ============
    // chat.jsp 把在线会话存在 application 的 "onlineSessions" 属性里，
    // 退出时要同步清掉，否则“在线用户”里还会残留当前用户。
    Map<String, Object[]> onlineSessions =
            (Map<String, Object[]>) application.getAttribute("onlineSessions");
    if (onlineSessions != null) {
        // 此时 session 尚未失效，可以安全调用 getId()
        onlineSessions.remove(session.getId());
    }
    // ============ 2. 让 session 失效 ============
    try {
        session.invalidate();
    } catch (IllegalStateException ignore) {
        // 会话已经失效，忽略
    }
    // ============ 3. 跳转由下面的 <c:redirect> 完成 ============
%>
<%--
    c:redirect 会把以 “/” 开头的 url 当作当前 Web 应用的相对路径，
    自动补上 contextPath。这里已经在 url 中显式拼上 contextPath，
    因此用 context="/" 阻止它再次拼接，避免出现
    /jspchatroom/jspchatroom/login.jsp 这样的重复路径。
--%>
<c:redirect url="${pageContext.request.contextPath}/login.jsp" context="/"/>
