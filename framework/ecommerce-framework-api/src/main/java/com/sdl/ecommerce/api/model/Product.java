package com.sdl.ecommerce.api.model;

import java.util.List;
import java.util.Map;

/**
 * E-Commerce Product
 *
 * @author nic
 */
public interface Product {

    /**
     * Get product ID
     * @return id
     */
    String getId();

    /**
     * Get master product ID. If this product is a variant, this ID could point to the master product.
     * @return master ID
     */
    String getMasterId();

    /**
     * Get product variant ID. If NULL no variant ID is available for current product, i.e. is a master product.
     * Variant ID is normally referencing to a concrete product selection with a particular color, size etc.
     * @return variant id
     */
    String getVariantId();

    /**
     * Get name of the product. This can be a localized name based on current language.
     * @return name
     */
    String getName();

    /**
     * Get product price.
     * @return price
     */
    ProductPrice getPrice();

    /**
     * Get product description. This can be localized based on the current language.
     * @return description
     */
    String getDescription();

    // TODO: Have images based on type, primary, thumbnail, zoom etc

    /**
     * Get URL to thumbnail image.
     * @return url
     */
    String getThumbnailUrl();

    /**
     * Get URL to primary image (used on detail pages etc).
     * @return url
     */
    // TODO: How to handle image links? It should be possible to configure if the image should be proxied by the connector (if the E-Com instance is not available externally)
    String getPrimaryImageUrl();

    /**
     * Get categories the product belong to.
     * @return list of categories
     */
    List<Category> getCategories();

    /**
     * Get all additional attributes of the product. Can for example be used in compare views etc.
     * @return list of attributes
     */
    List<ProductAttribute> getAttributes();

    /**
     * Get product variants (if any)
     * @return list of variants
     */
    List<ProductVariant> getVariants();

    /**
     * If current product is an variant this list is populated with selected variant attributes
     * @return list of variant attributes
     */
    List<ProductAttribute> getVariantAttributes();

    /**
     * Get all available variant attribute types for this product, e.g. color, size. The type also contain all possible values.
     * @return list of variant attribute types
     */
    List<ProductVariantAttributeType> getVariantAttributeTypes();

    VariantLinkType getVariantLinkType();

}
