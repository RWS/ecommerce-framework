<%@ page import="com.sdl.ecommerce.dxa.model.FacetsWidget" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dxa" uri="http://www.sdl.com/tridion-dxa" %>
<%@ taglib prefix="xpm" uri="http://www.sdl.com/tridion-xpm" %>
<jsp:useBean id="entity" type="com.sdl.ecommerce.dxa.model.FacetsWidget" scope="request"/>
<jsp:useBean id="markup" type="com.sdl.webapp.common.markup.Markup" scope="request"/>
<jsp:useBean id="screenWidth" type="com.sdl.webapp.common.api.ScreenWidth" scope="request"/>
<jsp:useBean id="linkResolver" type="com.sdl.ecommerce.api.ECommerceLinkResolver" scope="request"/>
<c:if test="${entity.categoryReference.categoryPath == 'EMPTY'}">
    <li class="mega-nav-link">
       <a href="" style="pointer-events: none;">&lt;EMPTY&gt;</a>
    </li>
</c:if>
<c:if test="${entity.categoryReference.category != null && not empty entity.facetGroups}">
<%

    int cols = entity.getFacetGroups().size()+entity.getRelatedPromotions().size();
    int colWidth = 12/cols;
    int noOfCols = cols*colWidth;
    int colRemainder = 0;
    if ( entity.getRelatedPromotions().size() > 0 ) {
        colRemainder = (12-noOfCols)/entity.getRelatedPromotions().size();
    }

    request.setAttribute("navWidth", cols*230);
    request.setAttribute("colWidth", colWidth);
    request.setAttribute("noOfCols", cols*colWidth);
    request.setAttribute("colRemainder", colRemainder);

%>
<li class="mega-nav-link">
    <a href="${entity.categoryReference.categoryUrl}">${entity.categoryReference.category.name}</a>
    <div class="mega-nav" style="width: ${navWidth}px;">
        <div class="row">
            <div class="col-sm-12">
                <div class="mega-nav-content">
                    <div class="row">
                        <c:forEach var="facetGroup" items="${entity.facetGroups}">
                            <div class="col-sm-${colWidth}">
                                <h5><i class="fa fa-cog"></i> ${facetGroup.title} <c:if test="${viewHelper.showEditControls(facetGroup)}"><a class="xpm-button popup-iframe" style="margin: 0px 8px;" href="${facetGroup.editUrl}"><i class="fa fa-pencil-square"></i></a></c:if></h5>
                                <ul class="list-unstyled">
                                    <c:forEach var="facet" items="${facetGroup.facets}">
                                        <li>
                                            <a href="${linkResolver.getAbsoluteFacetLink(facet, entity.categoryReference.categoryUrl)}">${facet.title}</a>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </c:forEach>
                        <c:if test="${entity.relatedPromotions.size() gt 0}">
                            <c:forEach var="promotion" items="${entity.relatedPromotions}" varStatus="status">
                                <c:if test="${status.index lt 2}">
                                    <div class="col-sm-${colWidth+colRemainder}">
                                        <c:set var="promotion" value="${promotion}" scope="request"/>
                                        <!-- TODO: Ignore image map promotions here?? -->
                                        <c:import url="/WEB-INF/Views/ECommerce/Entity/Partials/${entity.getPromotionViewName(promotion)}-MegaNav.jsp"/>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </div>
</li>
</c:if>
<c:if test="${entity.categoryReference.category != null && empty entity.facetGroups}">
    <li class="mega-nav-link">
        <a href="${entity.categoryReference.categoryUrl}">${entity.categoryReference.category.name}</a>
    </li>
</c:if>

