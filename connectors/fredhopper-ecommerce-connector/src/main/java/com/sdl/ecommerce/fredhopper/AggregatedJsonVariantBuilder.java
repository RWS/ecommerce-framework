package com.sdl.ecommerce.fredhopper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fredhopper.lang.query.*;
import com.fredhopper.lang.query.location.criteria.MultiValuedCriterion;
import com.fredhopper.webservice.client.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sdl.ecommerce.api.LocalizationService;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.impl.*;
import com.sdl.ecommerce.fredhopper.model.FredhopperProduct;
import static com.sdl.ecommerce.fredhopper.FredhopperHelper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * Aggregated JSON Variant Builder
 * Build up variant info based on a JSON structure that aggregates all variant attributes.
 *
 * Format: {"attribute1": "value1","attribute2": "value2"} {"attribute1": "value1"}
 * Where attribute name/value is of non-ML type
 *
 * @author nic
 */
@Component
public class AggregatedJsonVariantBuilder implements ProductVariantBuilder {

    // TODO: Introduce sorting of variant values. Either numerical or through defined order

    private static final Logger LOG = LoggerFactory.getLogger(AggregatedJsonVariantBuilder.class);
    
    @org.springframework.beans.factory.annotation.Value("${fredhopper.variant.aggregatedJsonAttributeName:#{null}}")
    private String aggregatedVariantAttributeName = null;

    @org.springframework.beans.factory.annotation.Value("${fredhopper.variant.variantIdAttributeName:#{null}}")
    private String variantIdAttributeName = null;

    private String primaryVariantAttributeName = null;

    // The exposed variants. The order is how variants gets selected, for example color->size
    // TODO: Do we need to take locale in consideration??
    @org.springframework.beans.factory.annotation.Value("${fredhopper.variant.exposedVariants:#{null}}")
    private String exposedVariantsString = null;

    private List<String> exposedVariants = null;

    @org.springframework.beans.factory.annotation.Value("${fredhopper.variant.ignoredValues:#{null}}")
    private String ignoredValuesString = null;
    private List<String> ignoredValues = null;

    @Autowired
    private FredhopperClient fredhopperClient;

    @Autowired
    LocalizationService localizationService;

    @Autowired
    private ProductCategoryService categoryService;

    private ThreadLocal<Map<String,String>> currentVariantAttributes = new ThreadLocal();

    private Cache<String, FredhopperProduct> cachedMasterProducts = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES).build();  // TODO: Have this cache time configurable

    @PostConstruct
    public void initialize() {

        if ( exposedVariantsString != null ) {
            this.exposedVariants = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(exposedVariantsString, ", ");
            while ( tokenizer.hasMoreTokens() ) {
                this.exposedVariants.add(tokenizer.nextToken());
            }
            if ( this.exposedVariants.size() > 0 ) {

                // Use the first exposed variant as primary one (e.g. style determiner)
                //
                this.primaryVariantAttributeName = this.exposedVariants.get(0);
            }
        }
        if ( ignoredValuesString != null ) {
            this.ignoredValues = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(ignoredValuesString, ", ");
            while ( tokenizer.hasMoreTokens() ) {
                this.ignoredValues.add(tokenizer.nextToken());
            }
        }
    }

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

        this.currentVariantAttributes.set(variantAttributes);
    }

    @Override
    public void buildVariants(FredhopperProduct product, Universe universe) {

        if ( aggregatedVariantAttributeName == null ) {
            LOG.error("Missing Fredhopper attribute name for aggregated variant info. No variant data is assembled.");
            return;
        }
        else if ( variantIdAttributeName == null ) {
            LOG.error("Missing Fredhopper SKU attribute name for aggregated variant info. No variant data is assembled.");
            return;
        }

        FredhopperProduct masterProduct = product;
        
        List<ProductAttribute> currentVariantAttributes = new ArrayList<>();

        // Get variant attributes
        //
        for ( String variantAttributeId : this.exposedVariants ) {
            Attribute variantAttribute = product.getFredhopperAttribute(variantAttributeId);
            if ( variantAttribute != null && variantAttribute.getValue().size() == 1 ) {
                Value variantValue = variantAttribute.getValue().get(0);
                currentVariantAttributes.add(new GenericProductAttribute(
                        variantAttributeId,
                        this.fredhopperClient.getAttributeName(universe, variantAttributeId),
                        new GenericProductAttributeValue(variantValue.getNonMl(), variantValue.getValue())
                ));
            }
        }
        Map<String,String> requestVariantAttributes = this.currentVariantAttributes.get();


        if ( requestVariantAttributes != null ) {

            masterProduct = this.getMasterProduct(product.getMasterId());

            // Get additional variant attributes based on the request attributes
            //
            for (String currentAttributeId :requestVariantAttributes.keySet() ) {
                String currentAttributeValueId = requestVariantAttributes.get(currentAttributeId);
                Attribute variantAttribute = masterProduct.getFredhopperAttribute(currentAttributeId);
                if ( variantAttribute != null ) {
                    for ( Value value : variantAttribute.getValue() ) {
                        if ( value.getNonMl().equals(currentAttributeValueId) ) {
                            currentVariantAttributes.add(new GenericProductAttribute(
                                    currentAttributeId,
                                    this.fredhopperClient.getAttributeName(universe, currentAttributeId),
                                    new GenericProductAttributeValue(value.getNonMl(), value.getValue())));
                        }
                    }
                }
            }
        }
        else if ( !product.getId().equals(product.getMasterId()) ) {
            masterProduct = this.getMasterProduct(product.getMasterId());
        }

        product.setVariantAttributes(currentVariantAttributes);
        List<ProductVariant> variants;
        if ( masterProduct.getVariants() == null ) {
            variants = this.getProductVariants(masterProduct, universe);
            product.setVariants(variants);
        }
        else {
            variants = masterProduct.getVariants(); // Already fetched before (a cached master product)
        }
        
        List<ProductVariantAttributeType> attributeTypes = new ArrayList<>();

        // TODO: Verify this code - does it really list all possible values (even those that are not available???)

        if (variants.size() > 0) {
            for ( String variantAttributeId : this.exposedVariants ) {
                List<ProductVariantAttributeValueType> values = new ArrayList<>();
                List<String> addedValues = new ArrayList<>();
                for (ProductVariant variant : variants) {
                    ProductAttribute variantAttribute = ((GenericProductVariant) variant).getAttribute(variantAttributeId);
                    if ( variantAttribute == null || variantAttribute.getValues().isEmpty() ) {
                        continue;
                    }
                    ProductAttributeValue variantValue = variantAttribute.getValues().get(0);
                    if (variantValue != null && !addedValues.contains(variantValue.getValue())) {
                        values.add(new GenericProductVariantAttributeValueType(variantValue.getValue(),
                                                                               variantValue.getPresentationValue(),
                                                                               isSelected(variantAttribute,
                                                                               product.getVariantAttributes()),
                                                                               masterProduct != product ? isApplicable(variantAttribute, variants, currentVariantAttributes) : true));
                        addedValues.add(variantValue.getValue());
                    }
                }
                String variantAttributeName = this.fredhopperClient.getAttributeName(universe, variantAttributeId);
                attributeTypes.add(new GenericProductVariantAttributeType(variantAttributeId, variantAttributeName, values));
            }

        }
        product.setVariantAttributeTypes(attributeTypes);

        // Remove the JSON attribute from the result
        //
        product.removeAttribute(this.aggregatedVariantAttributeName);
    }

    /**
     * Get product variants
     * @param product
     * @param universe
     * @return list of variants
     */
    private List<ProductVariant> getProductVariants(FredhopperProduct product, Universe universe) {

        List<ProductVariant> variants = new ArrayList<>();
        Attribute aggregatedVariants = product.getFredhopperAttribute(aggregatedVariantAttributeName);
        if (aggregatedVariants != null && aggregatedVariants.getValue().size() > 0) {

            String jsonValue = aggregatedVariants.getValue().get(0).getValue();

            // Convert to a proper JSON list
            //
            // TODO: Improve the algorithm here. Right now it is quite sensitive
            jsonValue = "[" + jsonValue.replace("} {", "},{") + "]";

            List<Map<String, String>> variantAttributeList;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                variantAttributeList = (List<Map<String, String>>) objectMapper.readValue(jsonValue, List.class);
            } catch (IOException e) {
                LOG.error("Invalid JSON for the aggregated variants.", e);
                return null;
            }

            for (Map<String, String> aggregatedVariant : variantAttributeList) {

                String variantId = null;
                List<ProductAttribute> variantAttributes = new ArrayList<>();
                for (String variantAttributeId : aggregatedVariant.keySet()) {
                    String variantAttributeValueId = aggregatedVariant.get(variantAttributeId);
                    if (variantAttributeId.equals(this.variantIdAttributeName)) {
                        variantId = variantAttributeValueId;
                    } else if ( this.exposedVariants.contains(variantAttributeId) && (this.ignoredValues == null || (this.ignoredValues != null && !this.ignoredValues.contains(variantAttributeValueId)) ) ) {

                        Attribute attribute = product.getFredhopperAttribute(variantAttributeId);
                        if (attribute != null) {
                            String variantAttributeValue = null;
                            for (Value value : attribute.getValue()) {
                                if (value.getNonMl().equals(variantAttributeValueId)) {
                                    variantAttributeValue = value.getValue();
                                }
                            }
                            String variantAttributeName = this.fredhopperClient.getAttributeName(universe, variantAttributeId);

                            // If value was found
                            //
                            if (variantAttributeValue != null) {
                                variantAttributes.add(new GenericProductAttribute(variantAttributeId, variantAttributeName,
                                        new GenericProductAttributeValue(variantAttributeValueId, variantAttributeValue)));
                            }
                        }

                    }
                }
                variants.add(new GenericProductVariant(variantId, null, variantAttributes));
            }
        }
        return variants;
    }

    /**
     * Check if specified attribute selected
     * @param variantAttribute
     * @param productVariantAttributes
     * @return if selected
     */
    private boolean isSelected(ProductAttribute variantAttribute, List<ProductAttribute> productVariantAttributes) {
        if (!variantAttribute.getValues().isEmpty()) {
            ProductAttributeValue selectedValue = variantAttribute.getValues().get(0);
            for (ProductAttribute productVariantAttribute : productVariantAttributes) {
                if (productVariantAttribute.getValues().isEmpty()) {
                    continue;
                }
                ProductAttributeValue variantValue = productVariantAttribute.getValues().get(0);
                if (productVariantAttribute.getId().equals(variantAttribute.getId()) &&
                        variantValue.getValue().equals(selectedValue.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if specified attribute applicable for current combination of variants
     * @param variantAttribute
     * @param allProductVariants
     * @param currentVariantAttributes
     * @return if applicable
     */
    private boolean isApplicable(ProductAttribute variantAttribute, List<ProductVariant> allProductVariants, List<ProductAttribute> currentVariantAttributes) {

        // TODO: Test with a product that does not have a style

        if ( variantAttribute.getId().equals(this.primaryVariantAttributeName) ) {
            return true;
        }
        List<ProductVariant> filteredList = new ArrayList<>();
        for ( ProductAttribute currentAttribute : currentVariantAttributes ) {
            if ( currentAttribute.getValues().isEmpty() ) {
                continue;
            }
            ProductAttributeValue currentValue = currentAttribute.getValues().get(0);
            if ( !currentAttribute.getId().equals(variantAttribute.getId()) ) {
                if ( filteredList.isEmpty() ) {
                    buildFilterList(filteredList, currentAttribute, currentValue, allProductVariants);
                }
                else {
                    // Further filter the filter list
                    //
                    List<ProductVariant> currentFilteredList = filteredList;
                    filteredList = new ArrayList<>();
                    buildFilterList(filteredList, currentAttribute, currentValue, currentFilteredList);
                }
            }
        }

        if ( !variantAttribute.getValues().isEmpty() ) {

            ProductAttributeValue variantValue = variantAttribute.getValues().get(0);
            // Is the value in the filtered list ?
            //
            for (ProductVariant variant : filteredList) {
                ProductAttribute attribute = ((GenericProductVariant) variant).getAttribute(variantAttribute.getId());
                if (attribute != null && !attribute.getValues().isEmpty()) {
                    ProductAttributeValue value = attribute.getValues().get(0);
                    if (value.getValue().equals(variantValue.getValue())) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    /**
     * Build filter list
     * @param filteredList
     * @param currentAttribute
     * @param currentValue
     * @param currentFilteredList
     */
    private void buildFilterList(List<ProductVariant> filteredList, ProductAttribute currentAttribute, ProductAttributeValue currentValue, List<ProductVariant> currentFilteredList) {
        for ( ProductVariant variant : currentFilteredList ) {
            ProductAttribute attribute = ((GenericProductVariant) variant).getAttribute(currentAttribute.getId());
            if ( attribute.getValues().isEmpty() ) {
                continue;
            }
            ProductAttributeValue value = attribute.getValues().get(0);
            if ( attribute != null && value.getValue().equals(currentValue.getValue()) ) {
                filteredList.add(variant);
            }
        }
    }

    /**
     * Get master product
     * @param masterId
     * @return product
     */
    private FredhopperProduct getMasterProduct(String masterId) {

        String cacheKey =  masterId + "-" + localizationService.getLocale();
        FredhopperProduct masterProduct = this.cachedMasterProducts.getIfPresent(cacheKey);
        if ( masterProduct == null ) {

            Query query = this.fredhopperClient.buildQuery(getUniverse(localizationService), getLocale(localizationService));
            query.addSecondId(masterId);
            //query.getLocation().addCriterion(new SingleValuedCriterion(this.masterIdAttributeName, masterId));
            query.setView(com.fredhopper.lang.query.ViewType.DETAIL);
            FredhopperDetailResult result = new FredhopperDetailResult(this.fredhopperClient.doQuery(query), (FredhopperLinkManager) this.fredhopperClient, null);
            result.setCategoryService(this.categoryService);
            result.setLocalizationService(this.localizationService);
            masterProduct = (FredhopperProduct) result.getProductDetail();
            this.cachedMasterProducts.put(cacheKey, masterProduct);
        }
        return masterProduct;
    }

}
