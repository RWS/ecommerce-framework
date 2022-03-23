<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dxa" uri="http://www.sdl.com/tridion-dxa" %>
<%@ taglib prefix="xpm" uri="http://www.sdl.com/tridion-xpm" %>
<jsp:useBean id="entity" type="com.sdl.ecommerce.dxa.model.SearchFeedbackWidget" scope="request"/>
<jsp:useBean id="markup" type="com.sdl.webapp.common.markup.Markup" scope="request"/>
<c:if test="${entity.querySuggestions != null and entity.querySuggestions.size() gt 0}">
    <div class="panel panel-default" style="padding: 16px;">
        <div ${markup.property(entity, "spellCheckLabel")}>
            ${entity.spellCheckLabel}
        </div>
        <div style="margin-left: 16px;">
            <i>${entity.querySuggestions[0].original} ->
                    ${entity.querySuggestions[0].suggestion}
                <c:forEach var="suggestion" items="${entity.querySuggestions}">
                    <span style="margin-right: 8px;"><a href="${entity.getSuggestionUrl(suggestion, localization)}" style="text-decoration: underline;">${suggestion.suggestion}<c:if test="${suggestion.estimatedResults != null}"> (${suggestion.estimatedResults})</c:if></a></span>
                </c:forEach>
            </i>
        </div>
    </div>
</c:if>