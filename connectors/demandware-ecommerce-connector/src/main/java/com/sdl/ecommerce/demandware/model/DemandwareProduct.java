package com.sdl.ecommerce.demandware.model;

import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.Product;
import com.sdl.ecommerce.api.model.ProductVariant;
import com.sdl.ecommerce.api.model.impl.GenericProductVariantAttribute;
import com.sdl.ecommerce.api.model.impl.GenericProductVariantAttributeType;
import com.sdl.ecommerce.api.model.impl.GenericProductVariantAttributeValueType;
import com.sdl.ecommerce.demandware.api.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demandware Product
 *
 * @author nic
 */
public class DemandwareProduct implements Product {

    private Category primaryCategory;
    private String id;
    private String name;
    private String description;
    private ProductPrice price;
    private String thumbnailUrl;
    private String primaryImageUrl;
    private List<ProductVariantAttribute> variantAttributes = null;
    private List<ProductVariant> variants = null;
    private List<ProductVariantAttributeType> variantAttributeTypes = null;

    /**
     * Create new E-Commerce API Product based on a Demandware API product
     * @param primaryCategory
     * @param dwreProduct
     */
    public DemandwareProduct(Category primaryCategory, com.sdl.ecommerce.demandware.api.model.Product dwreProduct) {
        this.primaryCategory = primaryCategory;
        this.id = dwreProduct.getId();
        this.name = dwreProduct.getName();
        this.description = dwreProduct.getLong_description();
        this.price = new DemandwarePrice(dwreProduct.getPrice(), dwreProduct.getCurrency());

        // TODO: Improve image handling to pick variant images in a better way

        this.thumbnailUrl = this.getImageUrl(dwreProduct, "small");
        if ( this.thumbnailUrl == null ) {
            // Fallback on large images
            //
            this.thumbnailUrl = this.getImageUrl(dwreProduct, "large");
        }
        this.primaryImageUrl = this.getImageUrl(dwreProduct, "large");

        // Get specific variation attributes (if this product is a variation)
        //
        if ( dwreProduct.getVariation_values() != null ) {
            this.variantAttributes = new ArrayList<>();
            for ( String attributeId : dwreProduct.getVariation_values().keySet() ) {
                String valueId = dwreProduct.getVariation_values().get(attributeId);
                for ( VariationAttribute variationAttribute : dwreProduct.getVariation_attributes() ) {
                    if ( attributeId.equals(variationAttribute.getId()) ) {
                        String attributeName = variationAttribute.getName();
                        for ( VariationAttributeValue value : variationAttribute.getValues() ) {
                            if ( valueId.equals(value.getValue()) ) {
                                ProductVariantAttribute attributeValue = new GenericProductVariantAttribute(attributeId,
                                        attributeName,
                                        valueId,
                                        value.getName());
                                this.variantAttributes.add(attributeValue);
                            }
                        }
                    }
                }
            }
        }

        // Get all available variations
        //
        if ( dwreProduct.getVariants() != null ) {
            if ( dwreProduct.getVariants().size() == 1 && dwreProduct.getVariants().get(0).getProduct_id().equals(this.id) ) {
                // Check if variants only contain the main product
                // -> Skip variations
                //
            }
            else {
                this.variants = new ArrayList<>();
                for ( com.sdl.ecommerce.demandware.api.model.ProductVariant variant : dwreProduct.getVariants() ) {
                    this.variants.add(new DemandwareProductVariant(variant, dwreProduct.getVariation_attributes(), dwreProduct.getCurrency()));
                }
                this.variantAttributeTypes = new ArrayList<>();
                for ( VariationAttribute attribute : dwreProduct.getVariation_attributes() ) {
                    List<ProductVariantAttributeValueType> values = new ArrayList<>();
                    for ( VariationAttributeValue variationAttributeValue : attribute.getValues() ) {
                        boolean isSelected = false;
                        if ( this.variantAttributes != null ) {
                            for ( ProductVariantAttribute selectedAttribute : this.variantAttributes ) {
                                if ( selectedAttribute.getId().equals(attribute.getId()) && selectedAttribute.getValueId().equals(variationAttributeValue.getValue())) {
                                    isSelected = true;
                                    break;
                                }
                            }
                        }
                        values.add(new GenericProductVariantAttributeValueType(variationAttributeValue.getValue(), variationAttributeValue.getName(), isSelected));
                    }
                    this.variantAttributeTypes.add(new GenericProductVariantAttributeType(attribute.getId(), attribute.getName(), values));
                }
            }
        }

    }

    /**
     * Create a new E-Commerce API Product based on a Demandware search result hit
     * @param productSearchHit
     */
    public DemandwareProduct(ProductSearchHit productSearchHit) {
        this.id = productSearchHit.getProduct_id();
        this.name = productSearchHit.getProduct_name();
        this.price = new DemandwarePrice(productSearchHit.getPrice(), productSearchHit.getCurrency());
        if ( productSearchHit.getImage() != null ) {
            this.thumbnailUrl = productSearchHit.getImage().getLink();
            this.primaryImageUrl = productSearchHit.getImage().getLink();
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ProductPrice getPrice() {
        return this.price;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    @Override
    public String getPrimaryImageUrl() {
        return this.primaryImageUrl;
    }

    /**
     * Get image for specified view type
     * @param dwreProduct
     * @param viewType
     * @return image
     */
    private String getImageUrl(com.sdl.ecommerce.demandware.api.model.Product dwreProduct, String viewType) {
        for (ImageGroup imageGroup : dwreProduct.getImage_groups() ) {
            if ( imageGroup.getView_type().equals(viewType) ) {
                return imageGroup.getImages().get(0).getLink();
            }
        }
        if ( dwreProduct.getImage_groups().size() > 0 ) {
            if ( dwreProduct.getImage_groups().get(0).getImages().size() > 0 ) {
                return dwreProduct.getImage_groups().get(0).getImages().get(0).getLink();
            }
        }
        return null;
    }

    @Override
    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        if ( this.primaryCategory != null ) {
            categories.add(this.primaryCategory);
        }
        return categories;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new HashMap<>();
    }

    @Override
    public List<ProductVariantAttribute> getVariantAttributes() {
        return this.variantAttributes;
    }

    @Override
    public List<ProductVariant> getVariants() {
        return this.variants;
    }

    @Override
    public List<ProductVariantAttributeType> getVariantAttributeTypes() {
        return this.variantAttributeTypes;
    }
}
