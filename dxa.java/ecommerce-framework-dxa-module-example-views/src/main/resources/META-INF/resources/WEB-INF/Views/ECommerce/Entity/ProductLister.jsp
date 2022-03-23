<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.sdl.webapp.common.api.ScreenWidth" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.sdl.ecommerce.api.model.Product" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dxa" uri="http://www.sdl.com/tridion-dxa" %>
<%@ taglib prefix="xpm" uri="http://www.sdl.com/tridion-xpm" %>
<jsp:useBean id="entity" type="com.sdl.ecommerce.dxa.model.ProductListerWidget" scope="request"/>
<jsp:useBean id="linkResolver" type="com.sdl.ecommerce.api.ECommerceLinkResolver" scope="request"/>
<jsp:useBean id="markup" type="com.sdl.webapp.common.markup.Markup" scope="request"/>
<jsp:useBean id="screenWidth" type="com.sdl.webapp.common.api.ScreenWidth" scope="request"/>
<div>

    <%
        final int cols = screenWidth == ScreenWidth.SMALL ? 2 : 4;
        final int rows = (int) Math.ceil(entity.getItems().size() / (double) cols);
        final Iterator<Product> iterator = entity.getItems().iterator();

        for (int row = 0; row < rows; row++) {
    %><div class="row row-eq-height"><%
    for (int col = 0; col < cols && iterator.hasNext(); col++) {
        final Product item = iterator.next();
        request.setAttribute("item", item);

    %><div class="col-sm-6 col-md-3 lister-product-teaser">

        <a href="${linkResolver.getProductDetailLink(item)}" class="lister-product-teaser-link">
            <div class="teaser">
                <img src="${item.thumbnailUrl}" class="teaser-img loader-img"/>
                <p class="teaser-description lister-product-name">
                    ${item.name}
                </p>
                <c:if test="${item.price != null}">
                    <h4 class="teaser-heading lister-product-price">${item.price.formattedPrice}</h4>
                </c:if>
            </div>
        </a>

    </div><%
        }
    %></div><%
        }
    %>


    <c:if test="${entity.showNavigation}">

        <%-- Simple standard DXA pagination
        <ul class="pagination lister-product-pagination">
            <c:if test="${entity.previousUrl != null}">
                <li><a href="${entity.previousUrl}"><i class="fa fa-angle-left"></i></a></li>
                <li><a href="${entity.previousUrl}">${entity.currentSet-1}</a></li>
            </c:if>
            <li class="active"><a href="#">${entity.currentSet}</a></li>
            <c:if test="${entity.nextUrl != null}">
                <li><a href="${entity.nextUrl}">${entity.currentSet+1}</a></li>
                <li><a href="${entity.nextUrl}"><i class="fa fa-angle-right"></i></a></li>
            </c:if>
        </ul>
        --%>

        <ul class="pagination lister-navigation">
            <c:if test="${entity.previousUrl != null}">
                <li><a href="${entity.previousUrl}"><i class="fa fa-angle-left"></i></a></li>
                <c:if test="${entity.firstUrl != null}">
                    <li><a href="${entity.firstUrl}">1</a></li>
                    <c:if test="${entity.currentSet gt 3}">
                        <li class="lister-navigation-break"><a>...</a></li>
                    </c:if>
                </c:if>
                <li><a href="${entity.previousUrl}">${entity.currentSet-1}</a></li>
            </c:if>
            <li class="active"><a href="#">${entity.currentSet}</a></li>
            <c:if test="${entity.nextUrl != null}">
                <li><a href="${entity.nextUrl}">${entity.currentSet+1}</a></li>
                <c:if test="${entity.lastUrl != null}">
                    <c:if test="${entity.currentSet+2 lt entity.viewSets}">
                        <li class="lister-navigation-break"><a>...</a></li>
                    </c:if>
                    <li><a href="${entity.lastUrl}">${entity.viewSets}</a></li>
                </c:if>
                <li><a href="${entity.nextUrl}"><i class="fa fa-angle-right"></i></a></li>
            </c:if>
        </ul>
    </c:if>
</div>
