<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<span id="onlineUserCount">${onlineUserCount}</span>
<span id="totalMessageCount">${totalMessages}</span>
<%@ include file="fragments/account-avatar.jspf" %>
<%@ include file="fragments/messages.jspf" %>
<%@ include file="fragments/online-users.jspf" %>
