package com.sdl.ecommerce.fredhopper;

import com.fredhopper.webservice.client.*;
import com.sdl.ecommerce.api.LocalizationService;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.ProductQueryService;
import com.sdl.ecommerce.api.QueryFilterAttribute;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.impl.GenericBreadcrumb;
import com.sdl.ecommerce.api.model.impl.GenericFacet;
import com.sdl.ecommerce.api.model.impl.GenericProductAttribute;
import com.sdl.ecommerce.api.model.impl.GenericProductAttributeValue;
import com.sdl.ecommerce.fredhopper.model.*;
import com.sdl.ecommerce.fredhopper.model.promotion.FredhopperPromotion;
import static com.sdl.ecommerce.fredhopper.FredhopperHelper.*;

import java.net.URLDecoder;
import java.util.*;

/**
 * FredhopperResultBase
 *
 * @author nic
 */
public abstract class FredhopperResultBase {

    protected Page fredhopperPage;
    protected Universe universe;
    protected FredhopperLinkManager linkManager;
    protected ProductCategoryService categoryService;
    protected ProductQueryService queryService;
    protected LocalizationService localizationService;
    protected Map<String,String> productModelMappings;
    protected List<String> hiddenFacetValues;
    protected List<String> aggregatedFacets;

    protected FredhopperResultBase(Page fredhopperPage, FredhopperLinkManager linkManager) {
        this.fredhopperPage = fredhopperPage;
        this.universe = this.getUniverse(fredhopperPage);
        this.linkManager = linkManager;
    }

    /****** Setters to inject the different services needed for getting additional data from the result set *****/

    void setQueryService(ProductQueryService queryService) {
        this.queryService = queryService;
    }

    void setCategoryService(ProductCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setLocalizationService(LocalizationService localizationService) {
        this.localizationService = localizationService;
        this.productModelMappings = getProductModelMappings(this.localizationService);
        this.hiddenFacetValues = getHiddenFacetValues(this.localizationService);
        this.aggregatedFacets =  getAggregatedFacets(this.localizationService);

    }

    /****** Helper functions to extract data from the Fredhopper universe *******/

    protected Universe getUniverse(Page page) {
        if ( page.getUniverses() != null ) {
            for (Universe u : page.getUniverses().getUniverse()) {
                if (UniverseType.SELECTED.equals(u.getType())) {
                    return u;
                }
            }
        }
        return null;
    }

    protected List<Product> getProducts(List<com.fredhopper.webservice.client.Item> items) {
        List<Product> products = new ArrayList<>();
        for ( com.fredhopper.webservice.client.Item item : items ) {
            FredhopperProduct fhItem = new FredhopperProduct(item, this.linkManager, this.productModelMappings, this.localizationService);
            List<ProductAttribute> attributes = new ArrayList<>();
            products.add(fhItem);
            for (Attribute attribute : item.getAttribute()) {
                String name = attribute.getName();
                Object value;
                if (attribute.getValue().size() == 0 ) {
                    continue;
                }
                String baseType = attribute.getBasetype().value();

                // TODO: Refactor this code!!!

                /*
                if ( baseType.equals("text") ) {
                    value = attribute.getValue().get(0).getValue();
                }
                else if (baseType.equals("int") ) {
                    value = Integer.parseInt(attribute.getValue().get(0).getValue());
                }
                else if (baseType.equals("float") ) {
                    value = Float.parseFloat(attribute.getValue().get(0).getValue());
                }
                */
                if (baseType.equals("set") || baseType.equals("list")) {
                    List<ProductAttributeValue> valueList = new ArrayList<>();
                    for (com.fredhopper.webservice.client.Value attrValue : attribute.getValue()) {
                        valueList.add(new GenericProductAttributeValue(attrValue.getNonMl(), attrValue.getValue()));
                    }
                    value = valueList;
                }
                else if (baseType.equals("cat")) {
                    List<ProductAttributeValue> valueList = new ArrayList<>();
                    for (com.fredhopper.webservice.client.Value attrValue : attribute.getValue()) {
                        valueList.add(new GenericProductAttributeValue(attrValue.getNonMl(), attrValue.getValue()));
                    }
                    value = valueList;
                }
                else {
                    value = new GenericProductAttributeValue(attribute.getValue().get(0).getNonMl(), attribute.getValue().get(0).getValue());
                }
                if ( value instanceof List ) {
                    List<ProductAttributeValue> currentValueList = getAttributeValues(attributes, name);
                    if ( currentValueList != null ) {
                        // Merge the list
                        //
                        List<ProductAttributeValue> list = (List<ProductAttributeValue>) value;
                        for ( ProductAttributeValue currentValue : currentValueList ) {
                            if ( !list.contains(currentValue) ) {
                                list.add(currentValue);
                            }
                        }
                    }
                }

                ProductAttribute productAttribute;
                if ( value instanceof ProductAttributeValue ) {
                    productAttribute = new GenericProductAttribute(name, FredhopperClient.getAttributeName(universe, name), (ProductAttributeValue) value);
                }
                else {
                    productAttribute = new GenericProductAttribute(name, FredhopperClient.getAttributeName(universe, name), (List<ProductAttributeValue>) value);
                }

                attributes.add(productAttribute);

                // Save the internal representation as well.
                // TODO: Merge this with the generic attribute representation
                //
                Attribute existingAttribute = fhItem.getFredhopperAttribute(name);
                if ( existingAttribute != null && existingAttribute.getBasetype() == attribute.getBasetype() ) {
                    for ( Value attributeValue : attribute.getValue() ) {
                        boolean alreadyExists = false;
                        for ( Value existingValue : existingAttribute.getValue() ) {
                            if ( existingValue.getNonMl().equals(attributeValue.getNonMl()) ) {
                                alreadyExists = true;
                            }
                        }
                        if ( !alreadyExists ) {
                            existingAttribute.getValue().add(attributeValue);
                        }
                    }
                }
                else {
                    if ( attribute.getValue().size() > 1 ) {
                        List<String> values = new ArrayList<>();
                        int i=0;
                        while( i < attribute.getValue().size() ) {
                            if ( values.contains(attribute.getValue().get(i).getNonMl())) {
                                attribute.getValue().remove(i);
                            }
                            else {
                                values.add(attribute.getValue().get(i).getNonMl());
                                i++;
                            }
                        }
                    }
                    fhItem.addFredhopperAttribute(name, attribute);
                }

            }
            fhItem.setAttributes(attributes);
        }
        return products;
    }

    protected static List<ProductAttributeValue> getAttributeValues(List<ProductAttribute> attributeList, String id) {
        for ( ProductAttribute attribute : attributeList ) {
            if ( attribute.getId().equals(id) ) {
                return attribute.getValues();
            }
        }
        return null;
    }

    protected List<Promotion> getPromotions(Universe universe) {
        return this.getPromotions(universe, null);
    }

    protected List<Promotion> getPromotions(Universe universe, List<QueryFilterAttribute> filterAttributes) {

        // For now just take the first theme list
        //
        if ( universe != null && !universe.getThemes().isEmpty() ) {
            List<Theme> themes = universe.getThemes().get(0).getTheme();
            List<Promotion> promotions = new ArrayList<>();
            for ( Theme theme : themes ) {
                if ( !include(theme.getCustomFields(), filterAttributes) ) {
                    continue;
                }
                // TODO: How to handle edit urls in a micro service environment? Point to a proxy service?
                String editUrl = "/fh-edit/campaigns.fh?" + URLDecoder.decode(universe.getLink().getUrlParams()) + "&id=" + theme.getId();
                List<Product> products = null;
                if ( theme.getItems() != null ) {
                    products = this.getProducts(theme.getItems().getItem());
                }
                promotions.add(FredhopperPromotion.build(theme, this.linkManager, products, editUrl, this.categoryService));
            }
            return promotions;
        }
        return Collections.EMPTY_LIST;
    }

    protected List<Breadcrumb> getBreadcrumbs(Universe universe, List<FacetParameter> currentFacets) {

        if (universe == null) {
            return Collections.EMPTY_LIST;
        }

        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        List<Crumb> fhCrumbs = universe.getBreadcrumbs().getCrumb();
        for ( Crumb fhCrumb : fhCrumbs ) {
            // Category
            //
            if (fhCrumb.getName().getAttributeType() != null && fhCrumb.getName().getAttributeType().equals("categories")) {
                String categoryId = fhCrumb.getName().getNonMlValue().replace("{", "").replace("}", "");

                // If searching using several categories (OR) -> Skip exposing that in the breadcrumb
                //
                if (categoryId.contains(",")) {
                    continue;
                }
                Category category = categoryService.getCategoryById(categoryId);
                Breadcrumb breadcrumb = new GenericBreadcrumb(fhCrumb.getName().getValue(), new CategoryRef(category));
                breadcrumbs.add(breadcrumb);
            }
            // Facet
            //
            else if (fhCrumb.getName().getAttributeType() != null) {

                String facetId = fhCrumb.getName().getAttributeType();

                if ( isHiddenFacet(facetId, currentFacets) ) {
                    // Do not show hidden facets in the breadcrumb
                    //
                    continue;
                }

                if (fhCrumb.getRange() != null && fhCrumb.getRange().getValueSet().size() == 2 && fhCrumb.getRange().getValueSet().get(0).getAggregation() == AggregationType.AND) {
                    com.fredhopper.webservice.client.Value minValue = fhCrumb.getRange().getValueSet().get(0).getEntry().get(0).getValue();
                    com.fredhopper.webservice.client.Value maxValue = fhCrumb.getRange().getValueSet().get(1).getEntry().get(0).getValue();
                    String facetTitle = minValue.getValue() + " - " + maxValue.getValue();
                    String facetValue = minValue.getNonMl() + "-" + maxValue.getNonMl();
                    Facet facet = new GenericFacet(facetId, facetTitle, facetValue, Facet.FacetType.RANGE);

                    Breadcrumb breadcrumb = new GenericBreadcrumb(facetTitle, facet);
                    breadcrumbs.add(breadcrumb);
                }
                // Aggregated facet values
                //
                // Temporary work-around to get most aggregated facets to work. Needs to be improved.
                //
                else if (this.aggregatedFacets != null && this.aggregatedFacets.contains(fhCrumb.getName().getAttributeType()) && fhCrumb.getRange() != null && fhCrumb.getRange().getValueSet().size() > 0 && fhCrumb.getRange().getValueSet().get(0).getAggregation() == AggregationType.OR) {
                    StringBuilder aggregatedValue = new StringBuilder();
                    List<Entry> entries = fhCrumb.getRange().getValueSet().get(0).getEntry();
                    for (int i = 0; i < entries.size(); i++) {
                        aggregatedValue.append(entries.get(i).getValue().getNonMl());
                        if (i < entries.size() - 1) {
                            aggregatedValue.append(";");
                        }
                    }
                    String title;
                    Filtersection section = this.getSelectedFilterSectionWithValue(universe, aggregatedValue.toString());
                    if (section != null) {
                        title = section.getLink().getName();
                    } else {
                        // If no aggregated title is found -> fall back on the first entry value
                        //
                        title = entries.get(0).getValue().getValue();
                    }
                    Facet facet = new GenericFacet(facetId, title, aggregatedValue.toString(), Facet.FacetType.MULTISELECT);
                    Breadcrumb breadcrumb = new GenericBreadcrumb(title, facet);
                    breadcrumbs.add(breadcrumb);
                } else {
                    StringTokenizer valueTokenizer = new StringTokenizer(fhCrumb.getName().getNonMlValue(), "{};");
                    StringTokenizer titleTokenizer = new StringTokenizer(fhCrumb.getName().getValue(), ";");
                    while (valueTokenizer.hasMoreTokens()) {
                        String facetValue = valueTokenizer.nextToken();
                        String facetTitle = titleTokenizer.nextToken().trim();
                        Facet facet = new GenericFacet(facetId, facetTitle, facetValue, Facet.FacetType.MULTISELECT);
                        Breadcrumb breadcrumb = new GenericBreadcrumb(facetTitle, facet);
                        breadcrumbs.add(breadcrumb);
                    }
                }
            }
        }
        return breadcrumbs;
    }

    protected boolean isHiddenFacet(String facetId, List<FacetParameter> currentFacets) {
        for ( FacetParameter facet : currentFacets ) {
            if ( facet.getName().equals(facetId) && facet.isHidden() ) {
                return true;
            }
        }
        return false;
    }

    protected Filtersection getSelectedFilterSectionWithValue(Universe universe, String value) {
        if (universe != null) {
            List<Filter> filters = this.getFacetFilters(universe);
            for (Filter filter : filters) {
                for (Filtersection section : filter.getFiltersection()) {
                    if (section.isSelected() != null && section.isSelected() && value.equals(section.getValue().getValue())) {
                        return section;
                    }
                }
            }
        }
        return null;
    }

    protected List<FacetGroup> getFacetGroups(Universe universe) {
        return this.getFacetGroups(universe, null);
    }

    protected List<FacetGroup> getFacetGroups(Universe universe, List<QueryFilterAttribute> queryFilterAttributes) {

        List<FacetGroup> facetGroups = new ArrayList<>();
        if (universe != null) {
            List<Filter> filters = this.getFacetFilters(universe);
            for (Filter filter : filters) {
                if (!include(filter.getCustomFields(), queryFilterAttributes)) {
                    continue;
                }
                // TODO: Move edit URL generation to somewhere else!!
                String editUrl = "/fh-edit/facets.fh?" + URLDecoder.decode(universe.getLink().getUrlParams() + "&id=" + filter.getFacetid());
                FacetGroup facetGroup = new FredhopperFacetGroup(filter, editUrl);
                facetGroups.add(facetGroup);

                for (Filtersection section : filter.getFiltersection()) {

                    if (!facetGroup.isCategory()) { // facet

                        // If a hidden facet value -> ignore to add it to the facet group
                        //
                        if (this.hiddenFacetValues != null && this.hiddenFacetValues.contains(section.getLink().getName())) {
                            continue;
                        }
                    }
                    Facet facet = new FredhopperFacet(filter, section);
                    facetGroup.getFacets().add(facet);
                }
            }
        }
        return facetGroups;
    }

    protected List<Filter> getFacetFilters(Universe universe) {
        List<Facetmap> facetmapArray = universe.getFacetmap();
        List<Filter> filters = new ArrayList<>();
        if ( facetmapArray != null ) {
            for (Facetmap facetmap : facetmapArray) {
                if (facetmap.getFilter() != null && !facetmap.getUniverse().equals("search")) {
                    filters.addAll(facetmap.getFilter());
                }
            }
        }
        return filters;
    }

    protected boolean include(CustomFields customFields, List<QueryFilterAttribute> filterAttributes) {
        if ( filterAttributes != null ) {
            for (QueryFilterAttribute filterAttribute : filterAttributes) {
                if ( filterAttribute.getName() != null && filterAttribute.getValue() != null ) {
                    String customFieldValue = this.getCustomFieldValue(customFields, filterAttribute.getName());
                    if (!filterAttribute.getValue().equals(customFieldValue) && filterAttribute.getMode() == QueryFilterAttribute.FilterMode.INCLUDE) {
                        return false;
                    } else if (filterAttribute.getValue().equals(customFieldValue) && filterAttribute.getMode() == QueryFilterAttribute.FilterMode.EXCLUDE) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected String getCustomFieldValue(CustomFields customFields, String name) {
        if ( customFields != null ) {
            for (CustomField field : customFields.getCustomField()) {
                if (field.getName().equals(name)) {
                    return field.getValue();
                }
            }
        }
        return null;
    }

}
