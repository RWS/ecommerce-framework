package com.sdl.ecommerce.demandware;

import com.sdl.ecommerce.api.model.*;
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
 * Test Demandware Services
 *
 * The tests are based on the standard Demandware demo shop (Genesis)
 *
 * @author nic
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class, loader = AnnotationConfigContextLoader.class)
public class DemandwareConnectorIT extends GenericTestSuite {

    private static final Logger LOG = LoggerFactory.getLogger(DemandwareConnectorIT.class);

    @Test
    public void testGetCategoryById() throws Exception {

        this.testGetCategoryById("womens");
    }

    @Test
    public void testGetCategoryByPath() throws Exception {
        this.testGetCategoryByPath("/womens/clothing/tops");
    }

    @Test
    public void testGetProductsInCategory() throws Exception {
        this.testGetProductsInCategory("/womens/clothing/outfits");
    }

    @Test
    public void testGetCategoryFacets() throws Exception {
        this.testFacets("/womens");
    }

    @Test
    public void testSearch() throws Exception {
        this.testSearch("jewel");
    }

    @Test
    public void testSearchWithFacets() throws Exception {

        List<FacetParameter> facets = new ArrayList<>();
        facets.add(new FacetParameter("" +
                "", "Blue|Green"));
        this.testSearchWithFacets("shirt", facets);
    }

    @Test
    public void testGetProductDetail() throws Exception {
        this.testGetProductDetail("25720424");
    }

    @Test
    public void testGetProductVariants() throws Exception {
        LOG.info("Master product:");
        LOG.info("###################");
        this.testGetProductVariants("25720424");
        LOG.info("Variant product:");
        LOG.info("###################");
        this.testGetProductVariants("701644132798");
    }

    @Test
    public void testCart() throws Exception {
        this.testCart("013742002836", "013742002836");
    }

}
