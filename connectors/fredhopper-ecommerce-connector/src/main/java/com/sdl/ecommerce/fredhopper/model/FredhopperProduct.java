package com.sdl.ecommerce.fredhopper.model;

import com.fredhopper.webservice.client.Attribute;
import com.fredhopper.webservice.client.Item;
import com.sdl.ecommerce.api.LocalizationService;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.impl.GenericProductAttribute;
import com.sdl.ecommerce.fredhopper.FredhopperLinkManager;
import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Fredhopper Product
 *
 * @author nic
 */
public class FredhopperProduct implements Product {

    private Item item;
    private List<ProductAttribute> attributes = new ArrayList<>();
    private Map<String,Attribute> fhAttributes = new HashedMap();
    private List<Category> categories;
    private FredhopperLinkManager linkManager;
    private Map<String,String> modelMappings;
    private LocalizationService localizationService;

    private List<ProductVariant> variants;

    private List<ProductAttribute> variantAttributes;
    private List<ProductVariantAttributeType> variantAttributeTypes;

    /**
     * Create a Fredhopper product representation. The provided model mappings are used to map Fredhopper product attributes
     * to the standard E-Commerce Framework model
     * @param item
     * @param linkManager
     * @param modelMappings
     * @param localizationService
     */
    public FredhopperProduct(Item item,
                             FredhopperLinkManager linkManager,
                             Map<String,String> modelMappings,
                             LocalizationService localizationService) {
        this.item = item;
        this.linkManager = linkManager;
        this.modelMappings = modelMappings;
        this.localizationService = localizationService;
    }

    @Override
    public String getId() {
        return this.item.getId();
    }

    @Override
    public String getMasterId() {
        return this.getModelAttribute("masterId");
    }

    @Override
    public String getVariantId() {
        return this.getModelAttribute("variantId");
    }

    @Override
    public String getName() {
        return this.getModelAttribute("name", true);
    }

    @Override
    public String getDescription() {
        return this.getModelAttribute("description");
    }

    @Override
    public ProductPrice getPrice() {
        String fredhopperAttributeName = this.modelMappings.get("price");
        if ( fredhopperAttributeName != null ) {
            Attribute fhPrice = this.fhAttributes.get(fredhopperAttributeName);
            if ( fhPrice != null && !fhPrice.getValue().isEmpty() ) {
                float price = Float.parseFloat(fhPrice.getValue().get(0).getNonMl());
                String formattedPrice = fhPrice.getValue().get(0).getValue();
                return new FredhopperProductPrice(price, formattedPrice);
            }
        }
        return null;
    }

    @Override
    public String getThumbnailUrl() {
        String thumbnailUrl = this.getModelAttribute("thumbnailUrl");
        if ( thumbnailUrl == null ) {
            return this.getPrimaryImageUrl();
        }
        return this.linkManager.processImageUrl(thumbnailUrl);
    }

    @Override
    public String getPrimaryImageUrl() {
        String imageUrl = this.getModelAttribute("primaryImageUrl");
        if ( imageUrl != null ) {
            imageUrl = this.linkManager.processImageUrl(imageUrl);
        }
        return imageUrl;
    }

    @Override
    public List<Category> getCategories() {
        if ( this.categories == null ) {
            synchronized (this) {
                if (this.categories == null) {
                    this.categories = new ArrayList<>();
                }
            }
        }
        return this.categories;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        List<ProductAttribute> fullList = this.attributes;
        List<ProductAttribute> processedAttributes = new ArrayList<>();

        for ( ProductAttribute attribute : fullList ) {
            String modelAttributeName = getModelAttributeMapping(attribute.getId());
            if ( modelAttributeName != null ) {
                if ( !PredefinedModelAttributes.isDefined(modelAttributeName) ) {
                    ProductAttribute mappedProductAttribute =
                            new GenericProductAttribute(modelAttributeName, attribute.getName(), attribute.getValues());
                    processedAttributes.add(mappedProductAttribute);
                }
            }
            else {
                processedAttributes.add(attribute);
            }
        }
        return processedAttributes;
    }

    public void setAttributes(List<ProductAttribute> attributes) {
        this.attributes = attributes;
    }

    public void removeAttribute(String attributeName) {
        for ( ProductAttribute attribute : this.attributes ) {
            if ( attribute.getId().equals(attributeName) ) {
                this.attributes.remove(attribute);
                break;
            }
        }
    }

    @Override
    public List<ProductVariant> getVariants() {
        return this.variants;
    }

    @Override
    public List<ProductAttribute> getVariantAttributes() {
        return this.variantAttributes;
    }

    @Override
    public List<ProductVariantAttributeType> getVariantAttributeTypes() {
        return this.variantAttributeTypes;
    }

    @Override
    public VariantLinkType getVariantLinkType() {
        return VariantLinkType.VARIANT_ATTRIBUTES;
    }

    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }

    public void setVariantAttributes(List<ProductAttribute> variantAttributes) {
        this.variantAttributes = variantAttributes;
    }

    public void setVariantAttributeTypes(List<ProductVariantAttributeType> variantAttributeTypes) {
        this.variantAttributeTypes = variantAttributeTypes;
    }

    public Item getFredhopperItem() {
        return this.item;
    }

    public void addFredhopperAttribute(String name, Attribute attribute) {
        this.fhAttributes.put(name, attribute);
    }

    public Attribute getFredhopperAttribute(String name) {
        return this.fhAttributes.get(name);
    }

    public boolean isModelAttribute(String fredhopperId) {
        return this.modelMappings.containsValue(fredhopperId);
    }

    public List<ProductAttributeValue> getAttributeValues(String id) {
        for ( ProductAttribute attribute : this.attributes ) {
            if ( attribute.getId().equals(id) ) {
                return attribute.getValues();
            }
        }
        return null;
    }

    private String getModelAttribute(String name) {
        return this.getModelAttribute(name, false);
    }

    private String getModelAttribute(String name, boolean usePresentationValue) {
        String fredhopperAttribute = this.modelMappings.get(name);

        String fredhopperStringValue = null;
        if ( fredhopperAttribute != null ) {
            List<ProductAttributeValue> values = getAttributeValues(fredhopperAttribute);
            if ( values != null && values.size() > 0 ) {
                fredhopperStringValue = usePresentationValue ? values.get(0).getPresentationValue() : values.get(0).getValue();
            }
        }
        return fredhopperStringValue;
    }

    private String getModelAttributeMapping(String fhName) {
        for ( String modelName : this.modelMappings.keySet() ) {
            String mappedFredhopperName = this.modelMappings.get(modelName);
            if ( mappedFredhopperName.equals(fhName) ) {
                return modelName;
            }
        }
        return null;
    }
}
