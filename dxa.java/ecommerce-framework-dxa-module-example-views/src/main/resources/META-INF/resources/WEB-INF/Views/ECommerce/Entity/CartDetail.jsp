<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dxa" uri="http://www.sdl.com/tridion-dxa" %>
<%@ taglib prefix="xpm" uri="http://www.sdl.com/tridion-xpm" %>
<jsp:useBean id="entity" type="com.sdl.ecommerce.dxa.model.CartWidget" scope="request"/>
<jsp:useBean id="markup" type="com.sdl.webapp.common.markup.Markup" scope="request"/>
<jsp:useBean id="linkResolver" type="com.sdl.ecommerce.api.ECommerceLinkResolver" scope="request"/>

<div class="content">
    <h3 style="margin-bottom: 24px;"><i class="fa fa-shopping-cart"></i> <dxa:resource key="e-commerce.shoppingCartTitle"/></h3>
    <c:choose>
        <c:when test="${entity.cartCount == 0}">
            <div><dxa:resource key="e-commerce.shoppingCartEmptyText"/></div>
        </c:when>
        <c:otherwise>
            <div class="row">
                <div>
                    <div class="list-group">
                        <div class="list-group-item">
                            <div class="row">
                                <div class="col-md-1"></div>
                                <div class="col-md-2"></div>
                                <div class="col-md-5"><dxa:resource key="e-commerce.productLabel"/></div>
                                <div class="col-md-2"><dxa:resource key="e-commerce.quantityLabel"/></div>
                                <div class="col-md-2"><dxa:resource key="e-commerce.priceLabel"/></div>
                            </div>
                        </div>
                        <c:forEach var="item" items="${entity.cart.items}">
                            <div class="list-group-item">
                                <div class="row">
                                    <div class="col-md-1" style="text-align: center;">
                                        <a href="#" class="remove-from-cart" data-localization-prefix="${localization.localizePath('/')}" data-product-id="${item.product.id}"><i class="fa fa-minus-square"></i></a>
                                    </div>
                                    <div class="col-md-2">
                                        <img src="${item.product.thumbnailUrl}" height="100"/>
                                    </div>
                                    <div class="col-md-5">
                                        <a href="${linkResolver.getProductVariantDetailLink(item.product)}"><strong>${item.product.name}</strong></a> <br/>
                                        <c:if test="${item.product.variantAttributes != null }">
                                            <c:forEach var="variantAttribute" items="${item.product.variantAttributes}">
                                                ${variantAttribute.name}: ${variantAttribute.value}<br/>
                                            </c:forEach>
                                        </c:if>
                                    </div>
                                    <div class="col-md-2">
                                        ${item.quantity}
                                    </div>
                                    <div class="col-md-2">
                                        ${item.price.formattedPrice}
                                    </div>
                                </div>
                                </a>
                            </div>
                        </c:forEach>
                        <div class="list-group-item">
                            <div class="row">
                                <div class="col-md-1"></div>
                                <div class="col-md-2"></div>
                                <div class="col-md-5"></div>
                                <div class="col-md-2"><strong><dxa:resource key="e-commerce.cartTotalLabel"/></strong></div>
                                <div class="col-md-2">${entity.cart.totalPrice.formattedPrice}</div>
                            </div>
                        </div>
                    </div>
                    <div style="float: right;">
                        <c:if test="${not empty entity.checkoutLink.url}">
                            <a class="btn btn-primary checkout-button" href="${entity.checkoutLink.url}"  ${markup.property(entity, "checkoutLink")}>${entity.checkoutLink.linkText} <i class="fa fa-chevron-right"></i></a>
                        </c:if>
                    </div>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</div>