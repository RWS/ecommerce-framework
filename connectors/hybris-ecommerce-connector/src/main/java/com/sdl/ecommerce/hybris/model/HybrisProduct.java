package com.sdl.ecommerce.hybris.model;

import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.impl.GenericProductAttribute;
import com.sdl.ecommerce.api.model.impl.GenericProductAttributeValue;
import com.sdl.ecommerce.hybris.api.model.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Hybris Product
 *
 * @author nic
 */
public class HybrisProduct implements Product {

    private com.sdl.ecommerce.hybris.api.model.Product hybrisProduct;
    private HybrisPrice price;
    private List<Category> categories = null;

    public HybrisProduct(com.sdl.ecommerce.hybris.api.model.Product hybrisProduct) {
        this.hybrisProduct = hybrisProduct;
        if ( this.hybrisProduct.getPrice() != null ) {
            this.price = new HybrisPrice(this.hybrisProduct.getPrice());
        }
    }

    @Override
    public String getId() {
        return this.hybrisProduct.getCode();
    }

    @Override
    public String getMasterId() {
        return null;
    }

    @Override
    public VariantLinkType getVariantLinkType() {
        return VariantLinkType.VARIANT_ID;
    }

    @Override
    public String getVariantId() {
        return null;
    }

    @Override
    public String getName() {
        return this.hybrisProduct.getName();
    }

    @Override
    public ProductPrice getPrice() {
        return this.price;
    }

    @Override
    public String getDescription() {
        return this.hybrisProduct.getDescription();
    }

    @Override
    public String getThumbnailUrl() {
        // Using product image primarily as thumbnail and as fallback use the thumbnail
        //
        String imageUrl = this.getImageUrl("product");
        if ( imageUrl == null ) {
            imageUrl = this.getImageUrl("thumbnail");
        }
        return imageUrl;
    }

    @Override
    public String getPrimaryImageUrl() {
        return this.getImageUrl("zoom"); // Select the biggest available image
    }

    private String getImageUrl(String format) {
        for (Image image : this.hybrisProduct.getImages() ) {
            if ( image.getFormat().equals(format) ) {
                return image.getUrl();
            }
        }
        return null;
    }

    @Override
    public List<Category> getCategories() {
        if ( this.categories == null ) {
            synchronized ( this ) {
                if ( this.categories == null ) {
                    this.categories = new ArrayList<>();
                }
            }
        }
        return this.categories;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        List<ProductAttribute> attributeList = new ArrayList<>();
        Map<String,Object> hybrisAttributes = this.hybrisProduct.getAdditionalProperties();
        for ( String attributeName : hybrisAttributes.keySet() ) {
            String attributeValue = hybrisAttributes.get(attributeName).toString();

            // Right now we use the name for both ID & name
            //
            ProductAttribute attribute = new GenericProductAttribute(attributeName, attributeName,
                    new GenericProductAttributeValue(attributeValue));
            attributeList.add(attribute);
        }
        return attributeList;
    }

    @Override
    public List<ProductVariant> getVariants() {
        return null;
    }

    @Override
    public List<ProductAttribute> getVariantAttributes() {
        return null;
    }

    @Override
    public List<ProductVariantAttributeType> getVariantAttributeTypes() {
        return null;
    }
}
