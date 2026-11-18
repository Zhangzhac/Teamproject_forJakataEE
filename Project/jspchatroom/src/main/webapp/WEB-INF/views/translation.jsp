<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${pageLang}" />
<fmt:setBundle basename="messages" />
<c:choose>
    <c:when test="${not empty translationErrorKey}">
        <div class="translation err"><fmt:message key="${translationErrorKey}"/></div>
    </c:when>
    <c:when test="${not empty translationError}">
        <div class="translation err"><c:out value="${translationError}"/></div>
    </c:when>
    <c:otherwise>
        <div class="translation"><span class="tr-lang">[<fmt:message key="${translationLanguage}"/>]</span> <c:out value="${translationText}"/></div>
    </c:otherwise>
</c:choose>
