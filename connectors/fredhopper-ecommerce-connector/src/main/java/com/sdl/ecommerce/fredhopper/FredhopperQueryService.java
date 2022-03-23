package com.sdl.ecommerce.fredhopper;

import com.sdl.ecommerce.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.sdl.ecommerce.fredhopper.FredhopperHelper.*;

/**
 * FredhopperQueryService
 *
 * @author nic
 */
@Component
public class FredhopperQueryService implements ProductQueryService {

    @Autowired
    private ProductCategoryService categoryService;

    @Autowired
    private FredhopperClient fredhopperClient;

    @Autowired
    private LocalizationService localizationService;

    @Override
    public Query newQuery() {
        return new FredhopperQuery();
    }

    @Override
    public QueryResult query(Query query) throws ECommerceException {
        QueryResult result = this.fredhopperClient.query(query,
                getUniverse(localizationService),
                getLocale(localizationService),
                query.getContextData());
                //getTriggers(localizationService));
        this.injectServices(result);
        return result;
    }

    private void injectServices(QueryResult result) {
        FredhopperQueryResult fredhopperResult = (FredhopperQueryResult) result;
        fredhopperResult.setQueryService(this);
        fredhopperResult.setCategoryService(this.categoryService);
        fredhopperResult.setLocalizationService(this.localizationService);
    }
}
