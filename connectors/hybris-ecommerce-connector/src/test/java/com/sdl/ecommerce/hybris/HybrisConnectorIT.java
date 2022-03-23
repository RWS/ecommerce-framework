package com.sdl.ecommerce.hybris;

import com.sdl.ecommerce.api.*;
import com.sdl.ecommerce.api.model.*;
import com.sdl.ecommerce.api.model.Breadcrumb;
import com.sdl.ecommerce.api.model.Cart;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.Facet;
import com.sdl.ecommerce.api.model.Product;
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
 * Test Hybris E-Commerce Connector
 *
 * Tests is based on the standard Hybris Electronics demo shop
 *
 * @author nic
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class, loader = AnnotationConfigContextLoader.class)
public class HybrisConnectorIT extends GenericTestSuite {

    private static final Logger LOG = LoggerFactory.getLogger(HybrisConnectorIT.class);

    @Test
    public void testGetCategoryById() throws Exception {
        this.testGetCategoryById("571");
    }

    @Test
    public void testGetCategoryByPath() throws Exception {
        this.testGetCategoryByPath("/cameras");
        this.testGetCategoryByPath("/cameras/film_cameras");
    }

    @Test
    public void testGetProductsInCategory() throws Exception {
        this.testGetProductsInCategory("/cameras");
    }

    @Test
    public void testSearch() throws Exception {
        this.testSearch("sony");
    }

    @Test
    public void testSearchWithFacets() throws Exception {
        List<FacetParameter> facets = new ArrayList<>();
        facets.add(new FacetParameter("price", "$50-$199.99"));
        this.testSearchWithFacets("sony", facets);
    }

    @Test
    public void testBreadcrumbs() throws Exception {
        this.testBreadcrumbs("/cameras");
    }

    @Test
    public void testGetProductDetail() throws Exception {
        this.testGetProductDetail("478828");
    }

    @Test
    public void testCart() throws Exception {
        //this.testCart("676442", "1978440_blue");
        this.testCart("300649633");
    }

}
