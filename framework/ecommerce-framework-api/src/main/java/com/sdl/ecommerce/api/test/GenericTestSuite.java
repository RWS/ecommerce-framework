package com.sdl.ecommerce.api.test;

import com.sdl.ecommerce.api.*;
import com.sdl.ecommerce.api.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Generic Test Suite for the E-Commerce API
 *
 * @author nic
 */
public abstract class GenericTestSuite {

    private static final Logger LOG = LoggerFactory.getLogger(GenericTestSuite.class);

    @Autowired(required = false)
    protected ProductCategoryService productCategoryService;

    @Autowired(required = false)
    protected ProductCategoryService categoryService;

    @Autowired(required = false)
    protected ProductQueryService queryService;

    @Autowired(required = false)
    protected ProductDetailService detailService;

    @Autowired(required = false)
    protected CartService cartService;

    @Autowired(required = false)
    protected ECommerceLinkResolver linkResolver;

    // TODO: Add some generic asserts here

    protected void testGetCategoryById(String categoryId) throws Exception {

        Category category = this.categoryService.getCategoryById(categoryId);
        LOG.info("Category ID: " + category.getId() + ", Name: " + category.getName());
        printCategories(category.getCategories());
    }

    protected void testGetNotFoundCategoryById(String categoryId) throws Exception {

        Category category = this.categoryService.getCategoryById(categoryId);
        if ( category == null ) {
            LOG.info("Category was not found.");
        }
        else {
            // Replace with assert
            //
            throw new Exception("Category was found! Should not happen!");
        }
    }

    protected void testGetCategoryByPath(String categoryPath) throws Exception {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        LOG.info("Category ID: " + category.getId() + ", Name: " + category.getName());
        printCategories(category.getCategories());
    }

    protected void testGetProductsInCategory(String categoryPath) throws Exception {

        Category category = this.categoryService.getCategoryByPath(categoryPath);
        LOG.info("Category ID: " + category.getId() + ", Name: " + category.getName());

        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10));
        this.printProducts(result);
        /* TODO: NEEDED???
        LOG.info("Next set of products =>");
        result = result.next();
        this.printProducts(result);
        LOG.info("Previous set of products =>");
        result = result.previous();
        this.printProducts(result);
        */
    }

    protected void testNavigateCategoryTree(String categoryPath) throws Exception {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        LOG.info("Navigate through subcategories:");
        for( Category subCategory : category.getCategories() ) {
            LOG.info("Category:" + subCategory.getName());
            QueryResult result = this.queryService.query(
                    this.queryService.newQuery().category(subCategory));
            this.printProducts(result);
        }
    }

    protected void testPromotions(String categoryPath) throws Exception {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10));
        this.printPromotions(result.getPromotions());
    }

    protected void testFacets(String categoryPath) throws Exception {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10));
        this.printFacets(result.getFacetGroups());
    }

    protected void testBreadcrumbs(String categoryPath) throws Exception {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10));
        this.printBreadcrumbs(result.getBreadcrumbs());
    }

    protected void testSearch(String searchPhrase) throws Exception {
       testSearch(searchPhrase, null);
    }

    protected void testSearch(String searchPhrase, String categoryUrlPrefix) throws Exception {
        QueryResult result = this.queryService.query(this.queryService.newQuery().searchPhrase(searchPhrase).viewSize(100));
        LOG.info("Total count: " + result.getTotalCount());
        this.printProducts(result);
        this.printFacets(result.getFacetGroups());
    }

    protected void testSearchWithFacets(String searchPhase, List<FacetParameter> facets) throws Exception {
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        searchPhrase(searchPhase).
                        viewSize(10).
                        facets(facets));
        LOG.info("Total count: " + result.getTotalCount());
        this.printProducts(result);
        this.printFacets(result.getFacetGroups());
    }

    protected void testQueryFlyout(String categoryPath) throws Exception {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10).
                        viewType(ViewType.FLYOUT));

        this.printFacets(result.getFacetGroups());
        this.printPromotions(result.getPromotions());
    }

    protected void testQueryWithFilterAttributes(String categoryPath, QueryFilterAttribute filterAttribute) {
        Category category = this.categoryService.getCategoryByPath(categoryPath);
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10).
                        filterAttribute(filterAttribute));
        this.printFacets(result.getFacetGroups());
        this.printPromotions(result.getPromotions());
    }

    protected void testGetProductDetail(String productId) throws Exception {
        LOG.info("Getting detail for product...");
        ProductDetailResult result = this.detailService.getDetail(productId);
        Product product = result.getProductDetail();
        LOG.info("Product ID: " + product.getId());
        LOG.info("Product Name: " + product.getName());
        LOG.info("Product Description: " + product.getDescription());
        LOG.info("Price: " + product.getPrice().getPrice());
        LOG.info("Detail Page URL:" + this.linkResolver.getProductDetailLink(product));
        LOG.info("Primary Image URL: " + product.getPrimaryImageUrl());
        LOG.info("Categories: ");
        for (Category category : product.getCategories()) {
            LOG.info("ID: " + category.getId() + " Name: " + category.getName());
        }
        LOG.info("Breadcrumbs: ");
        this.printBreadcrumbs(result.getBreadcrumbs());
        this.printPromotions(result.getPromotions());
        LOG.info("Attributes: ");
        for (ProductAttribute attribute : product.getAttributes()) {
            if ( attribute.isSingleValue() ) {
                if ( attribute.getValues().size() > 0 ) {
                    LOG.info("Name: " + attribute.getName() + " Value: " + attribute.getValues().get(0).getPresentationValue());
                }
            }
            else {
                LOG.info("Name: " + attribute.getName() + " Values: ");
                for ( ProductAttributeValue value : attribute.getValues() ) {
                    LOG.info("  " + value.getPresentationValue());
                }
            }
        }

    }

    protected void testGetNotFoundProductDetail(String productId) throws Exception {
        LOG.info("Getting detail for product...");
        ProductDetailResult result = this.detailService.getDetail(productId);
        Product product = result.getProductDetail();
        if ( product == null ) {
            LOG.info("Product not found!");
        }
        else {
            // Replace with assert
            //
            throw new Exception("Product was found! Should not happen!");
        }
    }

    protected void testGetProductVariants(String productId) throws Exception {
        LOG.info("Getting detail for product...");
        ProductDetailResult result = this.detailService.getDetail(productId);
        Product product = result.getProductDetail();
        LOG.info("Product ID: " + product.getId());
        LOG.info("Product Name: " + product.getName());
        LOG.info("Product Description: " + product.getDescription());
        if ( product.getVariantAttributes() != null ) {
            LOG.info("Current product is an variant with current attributes:");
            for ( ProductAttribute attribute : product.getVariantAttributes() ) {
                LOG.info("  " + attribute.getName() + " = " + attribute.getValues().get(0).getPresentationValue());
            }
        }
        if ( product.getVariants() != null ) {
            LOG.info("Variants: ");
            for (ProductVariant variant : product.getVariants()) {
                LOG.info("  Product Variant ID: " + variant.getId());
                LOG.info("  Price: " + variant.getPrice().getFormattedPrice());
                LOG.info("  Attributes:");
                for (ProductAttribute attribute : variant.getAttributes()) {
                    LOG.info("    " + attribute.getName() + " = " + attribute.getValues().get(0).getPresentationValue());
                }
            }
        }
        else if ( product.getVariantAttributes() != null ) {
            LOG.info("Variant attributes: ");
            for ( ProductAttribute attribute : product.getVariantAttributes() ) {
                LOG.info("  Attribute ID: " + attribute.getId());
                LOG.info("  Attribute Name: " + attribute.getName());
                LOG.info("  Attribute Value: " + attribute.getValues().get(0).getValue());
                LOG.info("  Attribute Presentation Value: " + attribute.getValues().get(0).getValue());
                LOG.info("      ---");
            }
        }
        if ( product.getVariantAttributeTypes() != null ) {
            LOG.info("Variant attribute types:");
            for ( ProductVariantAttributeType attributeType : product.getVariantAttributeTypes() ) {
                LOG.info("  Attribute Type: " + attributeType.getId() + " Name: " + attributeType.getName());
                LOG.info("  Values:");
                for ( ProductVariantAttributeValueType valueType : attributeType.getValues() ) {
                    LOG.info("    ID: " + valueType.getId() + " Value: " + valueType.getValue() + " Selected: " + valueType.isSelected());
                }
            }
        }
    }

    protected void testCart(String... productIds) throws Exception {
        LOG.info("Test cart...");

        Cart cart = this.cartService.createCart();

        for ( String productId : productIds ) {
            LOG.info("Adding product with ID: " + productId + " to cart...");
            cart = cartService.addProductToCart(cart.getId(), cart.getSessionId(), productId, 1);
            this.printCartItems(cart);
        }
    }

    /******** SUPPORT PRINTOUT FUNCTIONS ***********/

    protected void printCategories(List<Category> categories) {
        LOG.info("------ Categories: ---------");
        for ( Category subCategory : categories ) {
            LOG.info("Category ID: " + subCategory.getId() + ", Name: " + subCategory.getName());
        }
    }

    protected void printProducts(QueryResult resultSet) {
        LOG.info("------ Products: ---------");
        for ( Product product : resultSet.getProducts() ) {
            LOG.info("  Product ID: " + product.getId() + " Name: " + product.getName());
        }
    }

    protected void printPromotions(List<Promotion> promotions) {
        LOG.info("------ Promotions: -------");
        for ( Promotion promotion : promotions ) {
            LOG.info("Promo ID: " + promotion.getId() + " Name: "  + promotion.getName() + " Title: " + promotion.getTitle());
            if ( promotion instanceof ProductsPromotion ) {
                ProductsPromotion productsPromotion = (ProductsPromotion) promotion;
                LOG.info("  Products :");
                for ( Product product : productsPromotion.getProducts() ) {
                    LOG.info("  Product ID: " + product.getId() + " Name: " + product.getName());
                }
            }
            else if ( promotion instanceof ImageMapPromotion ) {
                ImageMapPromotion imageMapPromotion = (ImageMapPromotion) promotion;
                LOG.info("  Content Areas :");
                for (ImageMapPromotion.ContentArea contentArea : imageMapPromotion.getContentAreas() ) {
                    LOG.info("    (" + contentArea.getX1() + "," + contentArea.getY1() + "),(" + contentArea.getX2() + "," + contentArea.getY2() + ") -> " + this.linkResolver.getLocationLink(contentArea.getLocation()));
                }
            }
            else if ( promotion instanceof ContentPromotion ) {
                ContentPromotion contentPromotion = (ContentPromotion) promotion;
                LOG.info("  Content Promotion with image URL: " + contentPromotion.getImageUrl() + " location URL: " +  this.linkResolver.getLocationLink(contentPromotion.getLocation()));
            }
        }
    }

    protected void printFacets(List<FacetGroup> facetGroups) {
        LOG.info("------- Facets: ----------");
        for ( FacetGroup facetGroup : facetGroups ) {
            LOG.info("Facet group title: " + facetGroup.getTitle() + ", type:" + facetGroup.getType());
            LOG.info("--------------------------------------------");
            for ( Facet facet : facetGroup.getFacets() ) {
                LOG.info(facet.getTitle() + " (" + facet.getCount() + ") URL: " + this.linkResolver.getFacetLink(facet));
            }
            LOG.info("");
        }
    }

    protected void printBreadcrumbs(List<Breadcrumb> breadcrumbs) {
        LOG.info("------- Breadcrumbs: ---------");
        for ( Breadcrumb breadcrumb : breadcrumbs ) {
            LOG.info("\"" + breadcrumb.getTitle() + "\"" + " URL: " + this.linkResolver.getBreadcrumbLink(breadcrumb) + " category: " + breadcrumb.isCategory());
        }
    }

    protected void printCartItems(Cart cart) {
        LOG.info("Cart items:");
        for ( CartItem cartItem : cart.getItems() ) {
            LOG.info("Product: " + cartItem.getProduct().getName() + " Price: " + cartItem.getPrice().getFormattedPrice() + " Quantity: " + cartItem.getQuantity());
        }
        LOG.info("Total items: " + cart.count());
        LOG.info("Total price: " + cart.getTotalPrice().getFormattedPrice());
    }

}
