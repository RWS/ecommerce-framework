package com.sdl.ecommerce.dxa.model;

import com.sdl.ecommerce.api.model.*;

import java.util.List;
import java.util.Map;

/**
 * SimpleProduct
 *
 * @author nic
 */
public class SimpleProduct implements Product {

        private Product product;

        public SimpleProduct(Product product) {
            this.product = product;
        }

        public String getId() {
            return this.product.getId();
        }

        public String getName() {
            return this.product.getName();
        }

        public String getDescription() {
            return this.product.getDescription();
        }

        public String getThumbnailUrl() {
            return this.product.getPrimaryImageUrl();
        }

        public String getPrimaryImageUrl() {
            return this.product.getThumbnailUrl();
        }

        public List<ProductAttribute> getAttributes() {
            return this.product.getAttributes();
        }

        @Override
        public String getMasterId() {
            return this.product.getMasterId();
        }

        @Override
        public String getVariantId() {
            return this.product.getVariantId();
        }


        @Override
        public ProductPrice getPrice() {
            return this.product.getPrice();
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
