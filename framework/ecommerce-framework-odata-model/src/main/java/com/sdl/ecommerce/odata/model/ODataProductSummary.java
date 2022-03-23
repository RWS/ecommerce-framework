package com.sdl.ecommerce.odata.model;

import com.sdl.ecommerce.api.model.*;
import com.sdl.odata.api.edm.annotations.EdmComplex;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OData Product Summary
 *
 * @author nic
 */
@EdmComplex(name="ProductSummary", namespace = "SDL.ECommerce")
public class ODataProductSummary implements Product {


    @EdmProperty
    private String id;

    @EdmProperty
    private String masterId;

    @EdmProperty
    private String variantId;

    @EdmProperty
    private String name;

    @EdmProperty
    private ODataProductPrice price;

    @EdmProperty
    private String thumbnailUrl;

    @EdmProperty
    private List<ODataProductAttribute> attributes = new ArrayList<>();

    public ODataProductSummary() {}
    public ODataProductSummary(Product product) {
        this.id = product.getId();
        this.masterId = product.getMasterId();
        this.variantId = product.getVariantId();
        this.name = product.getName();
        if ( product.getPrice() != null ) {
            this.price = new ODataProductPrice(product.getPrice());
        }
        this.thumbnailUrl = product.getThumbnailUrl();
        if ( product.getAttributes() != null ) {
            product.getAttributes().forEach(attribute -> this.attributes.add(new ODataProductAttribute(attribute)));
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getMasterId() {
        return this.masterId;
    }

    @Override
    public String getVariantId() { return this.variantId; }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ProductPrice getPrice() {
        return this.price;
    }

    @Override
    public String getThumbnailUrl() {
        return this.thumbnailUrl;
    }

    @Override
    public List<ProductAttribute> getAttributes() {
        return this.attributes.stream().collect(Collectors.toList());
    }

    /********* Data only available for product details ***********/

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getPrimaryImageUrl() {
        return null;
    }


    @Override
    public List<Category> getCategories() {
        return null;
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

    @Override
    public VariantLinkType getVariantLinkType() {
        return null;
    }
}
