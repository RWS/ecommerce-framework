package com.sdl.ecommerce.dxa;

import com.sdl.ecommerce.api.ECommerceLinkResolver;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.model.*;
import com.sdl.webapp.common.api.WebRequestContext;
import com.sdl.webapp.common.api.localization.Localization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.FACETS;
import static com.sdl.ecommerce.dxa.ECommerceRequestAttributes.URL_PREFIX;

/**
 * DXA Link Strategy
 *
 * @author nic
 */
@Component
public class DXALinkResolver implements ECommerceLinkResolver {

    @Autowired
    private WebRequestContext webRequestContext;


    @Autowired
    private ProductCategoryService categoryService;

    // TODO: Have some kind of context bean with handy info!!

    @Autowired
    private HttpServletRequest request;

    @Override
    public String getCategoryLink(Category category) {
        String urlPrefix = this.getUrlPrefix(request);
        return urlPrefix + CategoryRef.getCategoryAbsolutePath(category);
    }

    @Override
    public String getNonContextualCategoryLink(Category category) {
        Localization localization = this.webRequestContext.getLocalization();
        String urlPrefix = localization.localizePath("/c"); // TODO Have the category prefix configurable
        return urlPrefix + CategoryRef.getCategoryAbsolutePath(category);
    }

    @Override
    public String getFacetLink(Facet facet) {
        String link;
        List<FacetParameter> selectedFacets = this.getFacets(request);
        if (facet.isCategory()) {
            Category category = this.categoryService.getCategoryById(facet.getValue());
            link = this.getCategoryLink(category);
            link += this.getFacetLink(selectedFacets);
        }
        else { // facet

            if (facet.isSelected()) {
                link = this.getRemoveFacetLink(facet, selectedFacets);
            } else {
                link = this.getAddFacetLink(facet, selectedFacets);
            }
        }
        return link;
    }

    @Override
    public String getAbsoluteFacetLink(Facet facet, String baseCategoryPath) {
        String link;
        if (facet.isCategory()) {

            Category category = this.categoryService.getCategoryById(facet.getValue());
            link = this.getNonContextualCategoryLink(category);
            link += this.getFacetLink((List<FacetParameter>) null);

        }
        else { // facet

            if (facet.isSelected()) {
                link = baseCategoryPath + this.getRemoveFacetLink(facet, null);
            } else {
                link = baseCategoryPath + this.getAddFacetLink(facet, null);
            }
        }
        return link;
    }

    @Override
    public String getBreadcrumbLink(Breadcrumb breadcrumb) {

        if ( breadcrumb.isCategory() ) {
            return this.getCategoryLink(breadcrumb.getCategoryRef());
        }
        else { // Facet
            List<FacetParameter> selectedFacets = this.getFacets(request);
            return this.getRemoveFacetLink(breadcrumb.getFacet(), selectedFacets);
        }
    }

    @Override
    public String getLocationLink(Location location) {

        if ( location == null ) { return ""; }
        if ( location.getStaticUrl() != null ) {
            return location.getStaticUrl();
        }
        if ( location.getProductRef() != null ) {
            return this.getProductDetailLink(location.getProductRef().getId(), location.getProductRef().getName());
        }
        StringBuilder link = new StringBuilder();
        if ( location.getCategoryRef() != null ) {
            String urlPrefix = this.getUrlPrefix(request);
            link.append(urlPrefix + location.getCategoryRef().getPath());
        }
        if ( location.getFacets() != null && !location.getFacets().isEmpty() ) {
            link.append("?");
            for ( int i=0; i < location.getFacets().size(); i++ ) {
                FacetParameter facet = location.getFacets().get(i);
                link.append(facet.toUrl());
                if ( i+1 < location.getFacets().size() ) {
                    link.append("&");
                }
            }
        }
        return link.toString();
    }

    @Override
    public String getProductVariantDetailLink(Product product)
    {
        String productId;
        if ( product.getVariantId() != null )
        {
            productId = product.getVariantId();
        }
        else
        {
            productId = product.getId();
        }
        return this.getProductDetailLink(productId, product.getName());
    }

    @Override
    public String getProductDetailLink(Product product) {

        // TODO: Add the possibility to resolve to a CMS based detail page using dynamic links

       return this.getProductDetailLink(product.getId(), product.getName());
    }

    @Override
    public String getProductDetailVariantLink(Product product, String variantAttributeId, String variantAttributeValueId) {

        String productId = product.getId();


        // TODO: Add isPrimary parameter here!!

        // TODO: VERIFY THIS CODE -> IS IT ALIGNED WITH .NET VERSION????

        if ( product.getVariantLinkType() == VariantLinkType.VARIANT_ATTRIBUTES && product.getVariantAttributeTypes() != null && product.getVariantAttributes() != null ) {

            Map<String, String> selectedAttributes = new HashMap<>();
            selectedAttributes.put(variantAttributeId, variantAttributeValueId);
            for ( ProductVariantAttributeType attributeType : product.getVariantAttributeTypes() ) {
                if ( !attributeType.getId().equals(variantAttributeId) ) {
                    for ( ProductVariantAttributeValueType valueType : attributeType.getValues() ) {
                        if ( valueType.isSelected() ) {
                            selectedAttributes.put(attributeType.getId(), valueType.getId());
                        }
                    }
                }
            }

            // Use the selected attributes to build a URL with the variant attributes as query parameters
            //
            String link = this.getProductDetailLink(productId, product.getName());
            boolean firstAttribute = true;
            for ( String selectedAttributeId : selectedAttributes.keySet() ) {
                if ( firstAttribute ) {
                    link += "?";
                    firstAttribute = false;
                }
                else {
                    link += "&";
                }
                link += selectedAttributeId + "=" + selectedAttributes.get(selectedAttributeId);
            }
            return link;
        }
        else if ( product.getVariantLinkType() == VariantLinkType.VARIANT_ID && product.getVariants() != null && product.getVariants().size() > 0 ) {

            Map<String, String> selectedAttributes = new HashMap<>();
            selectedAttributes.put(variantAttributeId, variantAttributeValueId);
            if ( product.getVariantAttributes() != null ) {
                for (ProductAttribute attribute : product.getVariantAttributes()) {
                    if (!attribute.getId().equals(variantAttributeId)) {
                        if ( attribute.getValues() != null && !attribute.getValues().isEmpty() ) {
                            selectedAttributes.put(attribute.getId(), attribute.getValues().get(0).getValue());
                        }
                    }
                }
            }

            // Get matching variant based on the selected attributes
            //
            for (ProductVariant variant : product.getVariants()) {
                int matchingAttributes = 0;
                for (String selectedAttributeId : selectedAttributes.keySet()) {
                    String selectedAttributeValueId = selectedAttributes.get(selectedAttributeId);
                    for (ProductAttribute attribute : variant.getAttributes()) {
                        if ( attribute.getValues() != null && !attribute.getValues().isEmpty() ) {
                            if (attribute.getId().equals(selectedAttributeId) && attribute.getValues().get(0).getValue().equals(selectedAttributeValueId)) {
                                matchingAttributes++;
                                break;
                            }
                        }
                    }
                }
                if (matchingAttributes == selectedAttributes.size()) {
                    productId = variant.getId();
                    break;
                }
            }
        }


        return this.getProductDetailLink(productId, product.getName());
    }

    protected String getProductDetailLink(String productId, String productName) {

        // Handle some special characters on product IDs
        //
        productId = productId.replace("+", "__plus__");

        if ( productName != null ) {
            // Generate a SEO friendly URL
            //
            String seoName = productName.toLowerCase().
                    replace(" ", "-").
                    replace("'", "").
                    replace("--", "").
                    replace("/", "-").
                    replace("®", "").
                    replace("&", "").
                    replace(".", "").
                    replace("+", "-").
                    replace("\"", "");
            return  webRequestContext.getLocalization().localizePath("/p/") + seoName + "/" + productId;
        }
        return  webRequestContext.getLocalization().localizePath("/p/") + productId;
    }

    protected String getCategoryLink(CategoryRef categoryRef) {
        String urlPrefix = this.getUrlPrefix(request);
        return urlPrefix + categoryRef.getPath();
    }

    protected static String getFacetLink(List<FacetParameter> selectedFacets) {
        if ( selectedFacets == null || selectedFacets.size() == 0 ) { return ""; }
        StringBuilder sb = new StringBuilder();
        boolean firstParam = true;
        for ( FacetParameter facet : selectedFacets ) {
            if ( facet.isHidden() ) {
                continue;
            }
            if ( firstParam ) {
                sb.append("?");
                firstParam = false;
            }
            else {
                sb.append("&");
            }
            sb.append(facet.toUrl());
        }
        return sb.toString();
    }

    protected static String getAddFacetLink(Facet facet, List<FacetParameter> selectedFacets) {

        StringBuilder sb = new StringBuilder();
        boolean firstParam = true;
        boolean foundFacet = false;
        if ( selectedFacets != null ) {
            for (FacetParameter facetParam : selectedFacets) {
                if ( facetParam.isHidden() ) {
                    continue;
                }
                if (firstParam) {
                    sb.append("?");
                    firstParam = false;
                } else {
                    sb.append("&");
                }
                if (facetParam.getName().equals(facet.getId()) && !facetParam.containsValue(facet.getValue())) {
                    sb.append(facetParam.addValueToUrl(facet.getValue()));
                    foundFacet = true;
                } else {
                    sb.append(facetParam.toUrl());
                }
            }
        }
        if ( !foundFacet ) {
            if ( firstParam ) {
                sb.append("?");
            }
            else {
                sb.append("&");
            }
            sb.append(getFacetUrl(facet));
        }
        return sb.toString();
    }

    protected static String getRemoveFacetLink(Facet facet, List<FacetParameter> selectedFacets) {

        StringBuilder sb = new StringBuilder();
        boolean firstParam = true;
        if ( selectedFacets != null ) {
            for (FacetParameter facetParam : selectedFacets) {
                if ( facetParam.isHidden() ) {
                    continue;
                }
                if (firstParam) {
                    sb.append("?");
                    firstParam = false;
                } else {
                    sb.append("&");
                }
                if (facetParam.getName().equals(facet.getId()) && facetParam.containsValue(facet.getValue())) {
                    sb.append(facetParam.removeValueToUrl(facet.getValue()));
                } else {
                    sb.append(facetParam.toUrl());
                }
            }
        }
        return sb.toString();
    }

    protected static String getFacetUrl(Facet facet) {

        // TODO: This is already done by the FacetParameter class...

        String name = facet.getId();
        String value = facet.getValue();
        switch ( facet.getType() ) {
            case RANGE:
                value = facet.getValues().get(0) + "-" + facet.getValues().get(1);
                break;
            case MULTISELECT:
                value = "";
                for ( int i=0; i < facet.getValues().size(); i++ ) {
                    String facetValue = facet.getValues().get(i);
                    value += facetValue;
                    if ( i < facet.getValues().size()-1 ) {
                        value += "|";
                    }
                }
                break;
            case LESS_THAN:
                value = "<" + facet.getValue();
                break;
            case GREATER_THAN:
                value = ">" + facet.getValue();
                break;
            case SINGLEVALUE:
            default:
                name += "_val";

        }

        return name + "=" + value;
    }

    // TODO: Have a generic helper for this!!!

    /**
     * Get current URL prefix (category or search result page).
     * @param request
     * @return prefix
     */
    protected String getUrlPrefix(HttpServletRequest request) {
        String urlPrefix = (String) request.getAttribute(URL_PREFIX);
        if ( urlPrefix == null ) {
            Localization localization = this.webRequestContext.getLocalization();
            urlPrefix = localization.localizePath("/c"); // fallback to default category URL prefix
        }
        return urlPrefix;
    }

    /**
     * Get facets from the HTTP request.
     * @param request
     * @return facets
     */
    protected List<FacetParameter> getFacets(HttpServletRequest request) {
        List<FacetParameter> facets = (List<FacetParameter>) request.getAttribute(FACETS);
        return facets;
    }


}
