package com.sdl.ecommerce.fredhopper;

import com.sdl.ecommerce.api.QueryFilterAttribute;
import com.sdl.ecommerce.api.QueryResult;
import com.sdl.ecommerce.api.ViewType;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetParameter;
import com.sdl.ecommerce.api.test.GenericTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Fredhopper Services
 *
 * The tests are based on the standard demo data set in Fredhopper.
 *
 * @author nic
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class, loader = AnnotationConfigContextLoader.class)
public class FredhopperConnectorIT extends GenericTestSuite {

    private static final Logger LOG = LoggerFactory.getLogger(FredhopperConnectorIT.class);

    @Test
    public void testGetCategory() throws Exception {
        this.testGetCategoryByPath("/women");
    }

    @Test
    public void testGetCategoryById() throws Exception {
        this.testGetCategoryById("catalog01_18661_128622");
    }

    @Test
    public void testNavigateCategoryTree() throws Exception {
        this.testNavigateCategoryTree("/women");
    }

    @Test
    public void testPromotions() throws Exception {
        // TODO: Validate so this test really returns promos
        this.testPromotions("/women");
    }

    @Test
    public void testPromotionsOnSpecificViewType() throws Exception {
        Category category = this.categoryService.getCategoryByPath("/women");
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10).
                        viewType(ViewType.SUMMARY));
        this.printPromotions(result.getPromotions());
    }

    @Test
    public void testFacets() throws Exception {
        this.testFacets("/women");
    }

    @Test
    public void testBreadcrumbs() throws Exception {
        this.testBreadcrumbs("/women/accessories/watches");
    }

    @Test
    public void testSearch() throws Exception {
        this.testSearch("women watch");
    }

    @Test
    public void testQueryFlyout() throws Exception {
        this.testQueryFlyout("/women");
    }

    @Test
    public void testQueryWithFilterAttributes() throws Exception {
        this.testQueryWithFilterAttributes("/women", new QueryFilterAttribute("flyout", "yes", QueryFilterAttribute.FilterMode.INCLUDE));
    }

    @Test
    public void testGetProductDetail() throws Exception {
        this.testGetProductDetail("008010231960");
    }

    /*
    @Test
    public void testGetProductVariants() throws Exception {
        this.testGetProductVariants("6674147");
    }
    */

    @Test
    public void testGetProductDetailViaVariantId() throws Exception {
        this.testGetProductDetail("0080102319");
    }

    @Test
    public void testQueryWithRedirect() throws Exception {
        LOG.info("Querying a category & facet that only gives one product item...");
        Category category = this.categoryService.getCategoryByPath("/women/accessories");
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        category(category).
                        viewSize(10).
                        facet(new FacetParameter("brand", "adidas")).
                        facet(new FacetParameter("spotlight", "this20week27s20brochure")));
        LOG.info("Redirect Location: " + this.linkResolver.getLocationLink(result.getRedirectLocation()));
    }

    @Test
    public void testSearchRedirect() throws Exception {
        LOG.info("Testing redirects that are triggered by specific search keywords...");
        QueryResult result = this.queryService.query(this.queryService.newQuery().searchPhrase("contact").viewSize(100));
        LOG.info("Redirect Location: " + result.getRedirectLocation());
    }

    @Test
    public void testMultipleCategories() throws Exception {
        LOG.info("Querying several categories (men OR women)");
        List<Category> categories = new ArrayList<>();
        categories.add(this.categoryService.getCategoryByPath("/men"));
        categories.add(this.categoryService.getCategoryByPath("/women"));
        QueryResult result = this.queryService.query(
                this.queryService.newQuery().
                        categories(categories).
                        viewSize(10));
        this.printProducts(result);
    }

}
