<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dxa" uri="http://www.sdl.com/tridion-dxa" %>
<%@ taglib prefix="xpm" uri="http://www.sdl.com/tridion-xpm" %>
<jsp:useBean id="entity" type="com.sdl.ecommerce.dxa.model.ProductDetailWidget" scope="request"/>
<jsp:useBean id="markup" type="com.sdl.webapp.common.markup.Markup" scope="request"/>
<jsp:useBean id="screenWidth" type="com.sdl.webapp.common.api.ScreenWidth" scope="request"/>
<jsp:useBean id="linkResolver" type="com.sdl.ecommerce.api.ECommerceLinkResolver" scope="request"/>
<jsp:useBean id="localization" type="com.sdl.webapp.common.api.localization.Localization" scope="request"/>

<div class="content" style="margin-bottom: 8px;">
    <div class="row">
        <div class="col-md-6" ${markup.property(entity, "image")}>
            <c:choose>
                <c:when test="${not empty entity.image && entity.image.fileSize gt 100}">
                    <img class="center-block teaser-img" width="100%" id="product-image" src="${entity.image.url}"/>
                </c:when>
                <c:when test="${not empty entity.product.primaryImageUrl}">
                    <xpm:if-enabled>
                    <!-- Start Component Field: {"XPath":"tcm:Content/custom:ProductDetailWidget/custom:image[1]"} -->
                    </xpm:if-enabled>
                    <img class="center-block teaser-img" width="100%" id="product-image" src="${entity.product.primaryImageUrl}"/>
                </c:when>
            </c:choose>
        </div>
        <div class="col-md-6">
            <h3 class="product-title" ${markup.property(entity, "title")}>
                <c:choose>
                    <c:when test="${not empty entity.title}">
                       ${entity.title}
                    </c:when>
                    <c:otherwise>
                        <xpm:if-enabled>
                            <!-- Start Component Field: {"XPath":"tcm:Content/custom:ProductDetailWidget/custom:title[1]"} -->
                        </xpm:if-enabled>
                        ${entity.product.name}
                    </c:otherwise>
                </c:choose>
            </h3>
            <div class="product-description">
                <div>
                    <div ${markup.property(entity, "description")}>
                        <c:choose>
                            <c:when test="${not empty entity.description}">
                                ${entity.description}
                            </c:when>
                            <c:otherwise>
                                <xpm:if-enabled>
                                    <!-- Start Component Field: {"XPath":"tcm:Content/custom:ProductDetailWidget/custom:description[1]"} -->
                                </xpm:if-enabled>
                                ${entity.product.description}
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <c:if test="${entity.product.variantAttributeTypes != null}">
                        <%
                            boolean isPrimaryVariant = true; // TODO: WHY DO I NEED THIS????
                        %>
                       <c:forEach var="variantAttributeType" items="${entity.product.variantAttributeTypes}">
                           <h4>${variantAttributeType.name}</h4>
                           <c:forEach var="valueType" items="${variantAttributeType.values}">
                               <a class="btn ${valueType.selected?'btn-info disabled':'btn-default'}" style="margin-bottom: 4px;" href="${linkResolver.getProductDetailVariantLink(entity.product, variantAttributeType.id, valueType.id)}">
                                    ${valueType.value}
                               </a>
                           </c:forEach>
                       </c:forEach>
                    </c:if>

                    <h3 class="center-block" style="margin-top: 16px;">
                        ${entity.product.price.formattedPrice}
                    </h3>

                </div>
                <div class="product-add-to-cart">
                    <a class="btn btn-primary add-to-cart-button <c:if test="${entity.product.variantId == null && entity.product.masterId != null}">disabled</c:if>"  data-localization-prefix="${localization.localizePath('/')}" data-product-id="${entity.product.variantId != null ? entity.product.variantId : entity.product.id}"><dxa:resource key="e-commerce.addToCartLabel"/> <i
                            class="fa fa-cart-plus"></i></a>
                </div>
            </div>
        </div>
    </div>
</div>


<!-- .NET CODE


The algorithm to get isPrimaryVariant seems to be not solid


@if (Model.Product.VariantAttributeTypes != null)
{
bool isPrimaryVariant = true;
foreach (var variantAttributeType in Model.Product.VariantAttributeTypes)
{
<h4>@variantAttributeType.Name</h4>
foreach (var valueType in variantAttributeType.Values)
{
if (valueType.IsAvailable)
{
<a class="btn @(valueType.IsSelected ? "btn-info disabled" : "btn-default")" style="margin-bottom: 4px;"
href="@ECommerceContext.LinkResolver.GetProductDetailVariantLink(Model.Product, variantAttributeType.Id, valueType.Id, isPrimaryVariant)">
@valueType.Value
</a>
}
}
isPrimaryVariant = false;
}
}

<h3 class="center-block" style="margin-top: 16px;">
    @Model.Product.Price.FormattedPrice
</h3>
<div class="product-add-to-cart">
    <a class="btn btn-primary add-to-cart-button  @(Model.Product.VariantId == null && Model.Product.MasterId != null ? "disabled" : "")" data-product-id="@(Model.Product.VariantId != null ? Model.Product.VariantId : Model.Product.Id)">
    @Html.Resource("e-commerce.addToCartLabel") <i class="fa fa-cart-plus"></i>
    </a>
</div>
</div>
</div>
</div>
</div>




-->


