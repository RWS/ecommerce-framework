package com.sdl.ecommerce.odata.service;

import com.google.common.collect.Lists;
import com.sdl.ecommerce.odata.function.*;
import com.sdl.ecommerce.odata.model.*;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.registry.ODataEdmRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * EntityServiceRegister
 *
 * @author nic
 */
@Component
public class EntityServiceRegister {

    private static final Logger LOG = LoggerFactory.getLogger(EntityServiceRegister.class);

    @Autowired
    private ODataEdmRegistry oDataEdmRegistry;

    // Share this with the client? Is that possible??

    @PostConstruct
    public void registerEntities() throws ODataException {
        LOG.debug("Registering E-Commerce entities");

        oDataEdmRegistry.registerClasses(Lists.newArrayList(

                // Entities & complex types
                //
                ODataCategory.class,
                ODataCategorySummary.class,
                ODataProduct.class,
                ODataProductSummary.class,
                ODataProductPrice.class,
                ODataProductAttribute.class,
                ODataProductAttributeValue.class,
                ODataFacetGroup.class,
                ODataFacet.class,
                ODataQuerySuggestion.class,
                ODataLocation.class,
                ODataCategoryRef.class,
                ODataProductRef.class,
                ODataPromotion.class,
                ODataProductsPromotion.class,
                ODataContentPromotion.class,
                ODataContentArea.class,
                ODataBreadcrumb.class,
                ODataCart.class,
                ODataCartItem.class,
                ODataProductVariant.class,
                ODataProductVariantAttributeType.class,
                ODataProductVariantAttributeValueType.class,

                // Functions
                //
                ProductQueryFunction.class,
                ProductQueryFunctionImport.class,
                ODataQueryResult.class,
                GetInContextEditMenuFunction.class,
                GetInContextEditMenuFunctionImport.class,
                ODataEditMenu.class,
                ODataMenuItem.class,
                CreateCartFunction.class,
                CreateCartFunctionImport.class,
                AddProductToCartFunction.class,
                AddProductToCartFunctionImport.class,
                RemoveProductFromCartFunction.class,
                RemoveProductFromCartFunctionImport.class,
                ProductVariantFunction.class,
                ProductVariantFunctionImport.class
        ));

    }
}

