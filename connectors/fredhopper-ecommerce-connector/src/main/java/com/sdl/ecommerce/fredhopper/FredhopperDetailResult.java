package com.sdl.ecommerce.fredhopper;

import com.fredhopper.webservice.client.*;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.impl.GenericBreadcrumb;
import com.sdl.ecommerce.fredhopper.model.FredhopperProduct;

import java.util.ArrayList;
import java.util.List;

/**
 * Fredhopper Detail Result
 *
 * @author nic
 */
public class FredhopperDetailResult extends FredhopperResultBase implements ProductDetailResult {

    private Product productDetail = null;
    private ProductVariantBuilder productVariantBuilder = null;

    public FredhopperDetailResult(Page fredhopperPage, FredhopperLinkManager linkManager, ProductVariantBuilder productVariantBuilder) {
        super(fredhopperPage, linkManager);
        this.productVariantBuilder = productVariantBuilder;
    }

    @Override
    public Product getProductDetail() {
        if ( this.productDetail == null ) {
            synchronized (this) {
                if (this.productDetail == null) {
                    this.productDetail = this.getProductDetail(this.universe);
                    // TODO: Throw a NOT_FOUND exception here!! Or have some kind of status code here???
                }
            }
        }
        return this.productDetail;
    }

    @Override
    public List<Breadcrumb> getBreadcrumbs() {
        return this.getProductBreadcrumbs(this.getProductDetail());
    }

    @Override
    public List<Promotion> getPromotions() {
        return this.getPromotions(this.universe);
    }

    private Product getProductDetail(Universe universe) {
        if ( universe != null && universe.getItemsSection() != null && universe.getItemsSection().getItems() != null ) {
            List<Product> products = this.getProducts(universe.getItemsSection().getItems().getItem());
            if (products.size() > 0) {
                FredhopperProduct product = (FredhopperProduct) products.get(0);
                // TODO: categoryId should also be an model attribute, right?

                List<ProductAttributeValue> categoryIds = product.getAttributeValues("categories");
                if (categoryIds != null) {
                    for (ProductAttributeValue categoryId : categoryIds) {
                        Category category = this.categoryService.getCategoryById(categoryId.getValue());
                        if (category != null) {
                            product.getCategories().add(category);
                        }
                    }
                }
                if ( this.productVariantBuilder != null ) {
                    this.productVariantBuilder.buildVariants(product, universe);
                }
                return product;
            }
        }
        return null;
    }

    private List<Breadcrumb> getProductBreadcrumbs(Product product) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();

        if ( product.getCategories() != null && product.getCategories().size() > 0 ) {
            Category category = product.getCategories().get(0);
            while (category != null) {
                breadcrumbs.add(0, new GenericBreadcrumb(category.getName(), new CategoryRef(category)));
                category = category.getParent();
            }
        }
        return breadcrumbs;
    }

}
