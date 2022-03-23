package com.sdl.ecommerce.fredhopper;

import com.fredhopper.lang.query.Query;
import com.fredhopper.lang.query.ViewType;
import com.fredhopper.lang.query.location.Location;
import com.fredhopper.lang.query.location.criteria.*;
import com.fredhopper.webservice.client.*;
import com.sdl.ecommerce.api.ProductCategoryService;
import com.sdl.ecommerce.api.ProductDetailResult;
import com.sdl.ecommerce.api.model.Category;
import com.sdl.ecommerce.api.model.FacetParameter;
import com.sdl.ecommerce.api.model.FacetParameter.ParameterType;
import com.sdl.ecommerce.api.QueryResult;
import com.sdl.ecommerce.api.model.impl.GenericLocation;
import com.sdl.ecommerce.fredhopper.model.FredhopperCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.ws.BindingProvider;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

/**
 * Fredhopper Client.
 * Supports FAS v7.5.x & FAS v8.1.x
 *
 * @author nic
 */
@Service
public class FredhopperClient implements FredhopperLinkManager {

    private static final Logger LOG = LoggerFactory.getLogger(FredhopperClient.class);

    // TODO: Use the REST Service here instead???

    static final String WS_PATH = "/fredhopper-ws/services/FASWebService";
    static final String WSDL_PATH = "/fredhopper-ws/services/FASWebService?wsdl";

    @org.springframework.beans.factory.annotation.Value("${fredhopper.queryserver.url}")
    private String fredhopperBaseUrl = "http://localhost:8180";

    @org.springframework.beans.factory.annotation.Value("${fredhopper.access.username}")
    private String username = null;

    @org.springframework.beans.factory.annotation.Value("${fredhopper.access.password}")
    private String password = null;

    @org.springframework.beans.factory.annotation.Value("${fredhopper.imageurl.mappings}")
    private String imageUrlMappingsConfig = null;
    private Map<String,String> imageUrlMappings = new HashMap<>();

    @org.springframework.beans.factory.annotation.Value("${fredhopper.variant.builder:#{null}}")
    private String productVariantBuilderName = null;
    
    private ProductVariantBuilder productVariantBuilder;

    @Autowired
    private ApplicationContext applicationContext;

    private FASWebService fasService;

    @PostConstruct
    public void initialize() throws Exception {
        URL wsdlURL = new URL(this.fredhopperBaseUrl + WSDL_PATH);

        if ( username != null && !username.isEmpty() ) {

            // TODO: Is this bullet-proof?? Will that impact other basic authentication connections???
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });
        }
        FASWebServiceService fasServiceService = new FASWebServiceService(wsdlURL);

        this.fasService = fasServiceService.getFASWebService();
        ((BindingProvider) this.fasService).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, fredhopperBaseUrl + WS_PATH);

        if ( imageUrlMappingsConfig != null ) {
            StringTokenizer tokenizer = new StringTokenizer(imageUrlMappingsConfig, "=,");
            while ( tokenizer.hasMoreTokens() ) {
                String imageUrlPrefix = tokenizer.nextToken();
                String imageUrlPrefixReplace = tokenizer.nextToken();
                imageUrlMappings.put(imageUrlPrefix, imageUrlPrefixReplace);
            }
        }

        // Build variant builder
        //
        if ( productVariantBuilderName != null ) {
            this.productVariantBuilder = (ProductVariantBuilder) this.applicationContext.getBean(productVariantBuilderName);
        }
    }

    /**
     * Get product detail.
     *
     * @param productId
     * @param universe
     * @param locale
     * @return
     */
    public ProductDetailResult getDetail(String productId, String universe, String locale) {
        return this.getDetail(productId, universe, locale, null);
    }

    /**
     * Get product detail using variant attributes.
     *
     * @param productId
     * @param universe
     * @param locale
     * @param variantAttributes
     * @return
     */
    public ProductDetailResult getDetail(String productId, String universe, String locale, Map<String,String> variantAttributes) {
        Query query = this.buildQuery(universe, locale);

        query.addSecondId(productId);

        if ( this.productVariantBuilder != null ) {
            this.productVariantBuilder.contributeToQuery(query, productId, variantAttributes);
        }
        query.setView(ViewType.DETAIL);
        return new FredhopperDetailResult(this.doQuery(query), this, this.productVariantBuilder);
    }

    public ProductDetailResult getDetailViaAttribute(String attributeId, String attributeValue, String universe, String locale) {
        Query query = this.buildQuery(universe, locale);
        query.getLocation().addCriterion(new SingleValuedCriterion(attributeId, attributeValue));
        query.setView(ViewType.DETAIL);
        return new FredhopperDetailResult(this.doQuery(query), this, this.productVariantBuilder);
    }

    public static String getAttributeName(Universe universe, String attributeId) {
        if ( universe != null && universe.getAttributeTypes() != null ) {
            for (AttributeType attributeType : universe.getAttributeTypes().getAttributeType()) {
                if (attributeType.getName().equals(attributeId)) {
                    return attributeType.getValue();
                }
            }
        }
        return null;
    }

    Page doQuery(Query query) {
        return this.doQuery(query, null);
    }
    Page doQuery(Query query, Map<String,String> triggers) {
        String triggerString = "";
        if ( triggers != null && !triggers.isEmpty() ) {
            for ( String trigger : triggers.keySet() ) {
                try {
                    triggerString += "&" + trigger + "=" + URLEncoder.encode(triggers.get(trigger), "UTF-8");
                }
                catch ( UnsupportedEncodingException e ) {}
            }

        }
        LOG.debug("Doing Fredhopper query: " + query.toQueryString());
        return this.fasService.getAll(query.toQueryString() + triggerString);
    }


    /**
     * Send a query request to Fredhopper.
     *
     * @param eCommerceQuery
     * @param universe
     * @param locale
     * @return query result
     */
    public QueryResult query(com.sdl.ecommerce.api.Query eCommerceQuery, String universe, String locale, Map<String,String> triggers) {

        Query query = this.buildQuery(universe, locale);
        if ( eCommerceQuery.getViewType() != null ) {
            query.setView(this.convertViewType(eCommerceQuery.getViewType()));
        }
        if ( eCommerceQuery.getSearchPhrase() != null ) {
            query.setSearchPhrase(eCommerceQuery.getSearchPhrase());
            query.setView(ViewType.SEARCH);
        }
        if (eCommerceQuery.getCategory() != null) {
            this.buildCategoryQuery(query, eCommerceQuery.getCategory());
        } else if (eCommerceQuery.getCategories() != null) {
            this.buildCategoryOrQuery(query, eCommerceQuery.getCategories());
        }
        this.buildFacetQuery(query, eCommerceQuery.getFacets());

        if ( eCommerceQuery.getStartIndex()  > 0 ) {
            query.setListStartIndex(eCommerceQuery.getStartIndex());
        }
        this.applyQueryConfiguration(query, (FredhopperQuery) eCommerceQuery);
        return new FredhopperQueryResult(this.doQuery(query, triggers), eCommerceQuery, this);
    }

    // TODO: Do a chain interface here instead

    public Query buildQuery(String universe, String locale) {
        Query query = new Query();
        Location location = new Location(universe, locale);
        query.setLocation(location);
        return query;
    }

    public void buildCategoryQuery(Query query, Category category) {
        List<String> categoryIds = new ArrayList<>();
        while ( category != null ) {
            categoryIds.add(0, category.getId());
            category = category.getParent();
        }
        for ( String categoryId: categoryIds ) {
            query.getLocation().addCriterion(new CategoryCriterion("categories", categoryId));
        }
    }

    public void buildCategoryOrQuery(Query query, List<Category> categories) {
        String categoryOrString = "";
        for ( Category category: categories ) {
            if (!categoryOrString.isEmpty()) {
                categoryOrString += ",";
            }
            categoryOrString += category.getId();
        }
        query.getLocation().addCriterion(new CategoryCriterion("categories", categoryOrString));
    }

    public void buildFacetQuery(Query query, List<FacetParameter> facets) {
        if ( facets != null ) {
            for (FacetParameter facet : facets) {
                query.getLocation().addCriterion(toCriterion(facet));
            }
        }
    }

    /**
     * Convert a customer facing facet parameter (part of the URL) to a Fredhopper criterion.
     *
     * @param facet
     * @return Fredhopper criterion
     */
    public Criterion toCriterion(FacetParameter facet) {
        Criterion criterion = null;
        if (facet.getType() ==ParameterType.MULTISELECT) {
            com.fredhopper.lang.query.location.criteria.ValueSet valueSet = new com.fredhopper.lang.query.location.criteria.ValueSet(com.fredhopper.lang.query.location.criteria.ValueSet.AggregationType.OR);
            for (String value : facet.getValues()) {
                valueSet.add(value);
            }
            criterion = new MultiValuedCriterion(facet.getName(), valueSet, null, false);
        }
        else if (facet.getType() == ParameterType.RANGE) {
            criterion = new SingleValuedCriterion(facet.getName(), facet.getValues().get(0), facet.getValues().get(1), false);
        }
        else if ( facet.getType() == ParameterType.LESS_THAN ) {
            criterion = new SingleValuedCriterion(facet.getName(), null, facet.getValues().get(0), false);
        }
        else if ( facet.getType() == ParameterType.GREATER_THAN ) {
            criterion = new SingleValuedCriterion(facet.getName(), facet.getValues().get(0), null, false);
        }
        else if ( facet.getType() == ParameterType.SINGLEVALUE) {
            criterion = new SingleValuedCriterion(facet.getName(), facet.getValues().get(0));
        }
        return criterion;
    }

    /**
     * Convert an Fredhopper criterion to a customer facing facet parameter (part of the URL).
     *
     * @param criterion
     * @return facet parameter
     */
    public FacetParameter fromCriterion(Criterion criterion) {
        String name = criterion.getAttributeName();
        String value = null;
        if ( criterion instanceof MultiValuedCriterion ) {
            MultiValuedCriterion mvCriterion = (MultiValuedCriterion) criterion;
            com.fredhopper.lang.query.location.criteria.ValueSet valueSet = mvCriterion.getLessThan();
            if ( valueSet == null ) {
                valueSet = mvCriterion.getGreaterThan();
            }
            for ( String val : valueSet.values() ) {
                if ( value == null ) {
                    value = val;
                }
                else {
                    value += "|" + val;
                }
            }
        }
        else  if ( criterion instanceof SingleValuedCriterion ) {
            SingleValuedCriterion svCriterion = (SingleValuedCriterion) criterion;
            if ( ! svCriterion.isEqual() ) { // range
                if ( svCriterion.getLessThan() == null ) {
                    value = ">" + svCriterion.getGreaterThan();
                }
                else if ( svCriterion.getGreaterThan() == null ) {
                    value = "<" + svCriterion.getLessThan();
                }
                else {
                    value = svCriterion.getLessThan() + "-" + svCriterion.getGreaterThan();
                }
            }
            else {
                value = svCriterion.getLessThan();
            }
        }
        return new FacetParameter(name, value);
    }

    /**
     * Convert from E-Commerce view type to Fredhopper view type.
     *
     * @param genericViewType
     * @return view type
     */
    private ViewType convertViewType(com.sdl.ecommerce.api.ViewType genericViewType) {
        ViewType viewType;
        switch (genericViewType) {
            case HOME:
                viewType = ViewType.HOME;
                break;
            case SUMMARY:
                viewType = ViewType.SUMMARY;
                break;
            case SEARCH:
                viewType = ViewType.SEARCH;
                break;
            case DETAIL:
                viewType = ViewType.DETAIL;
                break;
            case COMPARE:
                viewType = ViewType.COMPARE;
                break;
            case FLYOUT:
                viewType = ViewType.HOME;
                break;
            case LISTER:
            default:
                viewType =  ViewType.LISTER;
                break;
        }
        return viewType;
    }

    /**
     * Apply query configuration (normally set by various widgets)
     *
     * @param query
     * @param queryConfiguration
     */
    private void applyQueryConfiguration(Query query, FredhopperQuery queryConfiguration) {
        if ( queryConfiguration != null ) {
            if ( queryConfiguration.getViewSize() > 0 ) {
                query.setListViewSize(queryConfiguration.getViewSize());
            }
            if ( queryConfiguration.isDisableRedirect() != null ) {
                query.setDisableRedirect(queryConfiguration.isDisableRedirect());
            }
            if ( queryConfiguration.isForcedNaturalSorting() != null && queryConfiguration.isForcedNaturalSorting() == true ) {
                query.setForcedNaturalSorting();
            }
            if ( queryConfiguration.getFacetLimits() != null ) {
                for ( String facet : queryConfiguration.getFacetLimits().keySet() ) {
                    query.setFacetLimit(facet, queryConfiguration.getFacetLimits().get(facet));
                }
            }
            if ( queryConfiguration.getPageType() != null ) {
                query.setView(queryConfiguration.getPageType());
            }
        }
    }

    private int getCategoryLevel(String categoryId) {
        return (int) categoryId.chars().filter(ch -> ch == '_').count();
    }

    /**
     * Query using a specific category.
     *
     * @param baseQuery
     * @param category
     * @return Fredhopper page
     */
    public Page queryCategory(Query baseQuery, FredhopperCategory category) {
        Location location = new Location(baseQuery.getLocation());
        location.addCriterion(new CategoryCriterion("categories", category.getId()));
        Query query = new Query();
        query.setLocation(location);
        return this.doQuery(query);
    }

    /**
     * Get all categories belonging to specified parent category.
     *
     * @param parent
     * @param universe
     * @return list of categories
     */
    public List<Category> getCategories(Category parent, Universe universe) {
        List<Category> categories = new ArrayList<>();
        List facetmapArray = universe.getFacetmap();
        Facetmap facetmap = (Facetmap) facetmapArray.get(0);

        int level = parent != null && parent.getId() != null ? getCategoryLevel(parent.getId()) : 0;

        List<Filter> filters = facetmap.getFilter();
        for (Filter filter : filters) {
            if ( filter.getBasetype().value().equals("cat") && filter.isSelected() == null  ) {
                for (Filtersection section : filter.getFiltersection()) {
                    if (getCategoryLevel(section.getValue().getValue()) <= level)
                    {
                        // Parent category -> pick next
                        //
                        continue;
                    }

                    categories.add(new FredhopperCategory(parent, section));
                }
                break;
            }
        }
        return categories;
    }

    /**
     * Get Fredhopper location based on a specific category, search phrase, universe and locale.
     * The location are used for example in the in context edit popups.
     *
     * @param category
     * @param searchPhrase
     * @param universe
     * @param locale
     * @return location
     */
    public Location getLocation(Category category, String searchPhrase, String universe, String locale) {
        List<String> categoryIds = null;
        if ( category != null ) {
            categoryIds = new ArrayList<>();
            while (category != null) {
                categoryIds.add(0, category.getId());
                category = category.getParent();
            }
        }
        Location location = new Location(universe, locale);
        if ( categoryIds != null ) {
            for (String categoryId : categoryIds) {
                location.addCriterion(new CategoryCriterion("categories", categoryId));
            }
        }
        if ( searchPhrase != null ) {
            location.addCriterion(new SearchCriterion(searchPhrase));
        }
        return location;
    }

    /**
     * Get current universe from a Fredhopper page.
     *
     * @param page
     * @return universe
     */
    public Universe getUniverse(Page page) {
        for (Universe u : page.getUniverses().getUniverse()) {
            if (UniverseType.SELECTED.equals(u.getType())) {
                return u;
            }
        }
        return null;
    }

    /************ LINK MANAGER INTERFACE ***********************************/

    @Override
    public com.sdl.ecommerce.api.model.Location resolveLocation(String fhLocation, ProductCategoryService categoryService) {
        if ( fhLocation.contains("%") ) { // URL encoded location string
            try {
                fhLocation = URLDecoder.decode(fhLocation, "utf8");
            } catch ( UnsupportedEncodingException e  ) {}
        }
        Location fhLocationObj = new Location(fhLocation);

        String leafCategoryId = null;
        Category category = null;
        List<FacetParameter> facets = null;
        for ( Criterion criterion : (List<Criterion>) fhLocationObj.getAllCriteria() ) {

            if ( criterion.getAttributeName().equals("categories") ) {
                // Category
                //
                leafCategoryId = ((MultiValuedCriterion) criterion).getLessThan().values()[0];
            }
            else {
                // Facet
                //
                if ( facets == null ) {
                    facets = new ArrayList<>();
                }
                facets.add(fromCriterion(criterion));
            }
        }

        if ( leafCategoryId != null ) {
            category = categoryService.getCategoryById(leafCategoryId);
        }

        return new GenericLocation(category, facets);
    }

    @Override
    public String processImageUrl(String imageUrl) {

        for ( String imageUrlPrefix : this.imageUrlMappings.keySet() ) {
            if ( imageUrl.startsWith(imageUrlPrefix) ) {
                imageUrl = imageUrl.replace(imageUrlPrefix, imageUrlMappings.get(imageUrlPrefix));
                break;
            }
        }
        return imageUrl;
    }

}

