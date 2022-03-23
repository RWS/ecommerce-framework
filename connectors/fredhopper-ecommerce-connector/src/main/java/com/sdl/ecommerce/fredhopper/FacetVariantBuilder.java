package com.sdl.ecommerce.fredhopper;

import com.fredhopper.lang.query.Query;
import com.fredhopper.lang.query.location.criteria.MultiValuedCriterion;
import com.fredhopper.lang.query.location.criteria.SearchCriterion;
import com.fredhopper.webservice.client.*;
import com.sdl.ecommerce.api.model.ProductAttribute;
import com.sdl.ecommerce.api.model.ProductVariantAttributeType;
import com.sdl.ecommerce.api.model.ProductVariantAttributeValueType;
import com.sdl.ecommerce.api.model.impl.GenericProductAttribute;
import com.sdl.ecommerce.api.model.impl.GenericProductAttributeValue;
import com.sdl.ecommerce.api.model.impl.GenericProductVariantAttributeType;
import com.sdl.ecommerce.api.model.impl.GenericProductVariantAttributeValueType;
import com.sdl.ecommerce.fredhopper.model.FredhopperProduct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facet Variant Builder
 *
 * @author nic
 */
@Component
public class FacetVariantBuilder implements ProductVariantBuilder {

    @org.springframework.beans.factory.annotation.Value("${fredhopper.variant.prefix:#{null}}")
    private String variantPrefix = null;

    @Override
    public void contributeToQuery(Query query, String productId, Map<String, String> variantAttributes) {

        if ( variantAttributes != null ) {
            for ( String variantAttributeId : variantAttributes.keySet() ) {
                String variantAttributeValue = variantAttributes.get(variantAttributeId);

                com.fredhopper.lang.query.location.criteria.ValueSet valueSet = new com.fredhopper.lang.query.location.criteria.ValueSet(com.fredhopper.lang.query.location.criteria.ValueSet.AggregationType.OR);
                valueSet.add(variantAttributeValue);
                query.getLocation().addCriterion(new MultiValuedCriterion(variantAttributeId, valueSet, null, false));
                //query.getLocation().addCriterion(new SingleValuedCriterion(variantAttributeId, variantAttributeValue));
            }
        }

        // To get correct variant facets the product ID is also needed to inserted as a search criterion
        //
        query.getLocation().addCriterion(new SearchCriterion(productId));
    }

    @Override
    public void buildVariants(FredhopperProduct product, Universe universe) {
        
        // Get variant attribute types
        //
        List<ProductVariantAttributeType> attributeTypes = new ArrayList<>();
        List<Filter> filters = this.getFacetFilters(universe);
        for ( Filter filter : filters ) {
            if ( variantPrefix != null && filter.getOn().startsWith(variantPrefix) ) {
                List<ProductVariantAttributeValueType> values = new ArrayList<>();
                for (Filtersection section : filter.getFiltersection()) {
                    // TODO: Check if value is applicable or not
                    values.add(new GenericProductVariantAttributeValueType(section.getValue().getValue(), section.getLink().getName(), section.isSelected() != null ? section.isSelected() : false, true));
                }
                attributeTypes.add(new GenericProductVariantAttributeType(filter.getOn(), filter.getTitle(), values));
            }
        }
        if ( attributeTypes.isEmpty() ) {
            // Current product has no variants
            //
            return;
        }
        product.setVariantAttributeTypes(attributeTypes);

        // Get current variant values
        //
        List<ProductAttribute> variantAttributes = new ArrayList<>();
        for ( ProductVariantAttributeType attributeType : attributeTypes ) {
            Attribute attributeValue = product.getFredhopperAttribute(attributeType.getId());
            if ( attributeValue != null && !attributeValue.getValue().isEmpty() ) {
                Value value = attributeValue.getValue().get(0);
                variantAttributes.add(new GenericProductAttribute(attributeType.getId(), attributeType.getName(),
                        new GenericProductAttributeValue(value.getNonMl(), value.getValue())));
            }
        }
        product.setVariantAttributes(variantAttributes);
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
}
