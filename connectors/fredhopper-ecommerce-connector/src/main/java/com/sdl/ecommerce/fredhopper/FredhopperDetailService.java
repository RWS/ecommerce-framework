package com.sdl.ecommerce.fredhopper;

import com.sdl.ecommerce.api.*;
import com.sdl.ecommerce.api.model.Product;
import com.sdl.ecommerce.fredhopper.model.PredefinedModelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.sdl.ecommerce.fredhopper.FredhopperHelper.*;

/**
 * Fredhopper Detail Service
 *
 * @author nic
 */
@Component
public class FredhopperDetailService implements ProductDetailService {

    private static final Logger LOG = LoggerFactory.getLogger(FredhopperDetailService.class);

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private FredhopperClient fredhopperClient;

    @Autowired
    LocalizationService localizationService;

    @Override
    public ProductDetailResult getDetail(String productId) throws ECommerceException {
        ProductDetailResult result = this.fredhopperClient.getDetail(productId,
                                                                     getUniverse(localizationService),
                                                                     getLocale(localizationService));
        this.injectServices(result);

        if ( result.getProductDetail() == null ) {

            // Fallback on variant ID
            //
            Map<String,String> modelMappings = getProductModelMappings(localizationService);
            String variantId = modelMappings.get(PredefinedModelAttributes.variantId.name());
            if ( variantId != null ) {
                LOG.debug("Falling back on detail variant ID...");
                result = this.fredhopperClient.getDetailViaAttribute(variantId,
                                                                     productId,
                                                                     getUniverse(localizationService),
                                                                     getLocale(localizationService));
                this.injectServices(result);
            }
        }

        return result;
    }

    @Override
    public ProductDetailResult getDetail(String productId, Map<String, String> variantAttributes) throws ECommerceException {

        ProductDetailResult result = this.fredhopperClient.getDetail(productId,
                                                                     getUniverse(localizationService),
                                                                     getLocale(localizationService),
                                                                     variantAttributes);
        this.injectServices(result);
        Product product = result.getProductDetail();
        return result;
    }

    private void injectServices(ProductDetailResult result) {
        FredhopperDetailResult fredhopperResult = (FredhopperDetailResult) result;
        fredhopperResult.setCategoryService(this.categoryService);
        fredhopperResult.setLocalizationService(this.localizationService);
    }
}
