package com.sdl.ecommerce.demandware.api;

import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.demandware.api.model.*;
import com.sun.jersey.api.client.*;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * Demandware Shop Client v.16.8
 *
 * @author nic
 */
public class DemandwareShopClientImpl implements DemandwareShopClient {

    static private Logger log = LoggerFactory.getLogger(DemandwareShopClientImpl.class);

    // TODO: Update to 17.7 - Needs to update the cart implementation

    static final String BASE_URL_PATH = "/dw/shop/v16_4";

    private String shopUrl;
    private String clientId;
    private String locale;
    private String currency;
    private String overriddenOrigin = null;
    private boolean trustAllSSLCerts = false;

    private String shopBaseUrl;
    private Client client;
    private WebResource authorizationResource;
    private WebResource basketResource;
    private WebResource productResource;
    private WebResource productSearchResource;
    private WebResource categoryResource;

    /**
     * REST request builder
     */
    class Builder {

        WebResource.Builder requestBuilder;

        Builder(WebResource.Builder requestBuilder) {
            this.requestBuilder = requestBuilder;
        }

        WebResource.Builder getRequestBuilder() {
            return this.requestBuilder;
        }

        Builder jsonHeaders() {
            this.requestBuilder = requestBuilder.
                    accept(MediaType.APPLICATION_JSON).
                    type(MediaType.APPLICATION_JSON);
            return this;
        }

        Builder originHeader() {
            /*
            if ( overriddenOrigin != null && overriddenOrigin.length() > 0 ) {
                this.requestBuilder = requestBuilder.header("Origin", overriddenOrigin);
            }
            */
            return this;
        }

        Builder basketHeaders(Basket basket) {
            this.requestBuilder = requestBuilder.
                    header("Authorization", basket.getAuthorizationToken()).
                    header("If-Match", basket.getEtag());
            return this;
        }
    }

    /**
     * Createa new Demandware shop client.
     * @param shopUrl
     * @param clientId
     * @param locale
     * @param currency
     * @param overriddenOrigin
     * @param trustAllSSLCerts
     */
    public DemandwareShopClientImpl(String shopUrl,
                                    String clientId,
                                    String locale,
                                    String currency,
                                    String overriddenOrigin,
                                    boolean trustAllSSLCerts) {
        this.shopUrl = shopUrl;
        this.clientId = clientId;
        this.locale = locale;
        this.currency = currency;
        this.overriddenOrigin = overriddenOrigin;
        this.trustAllSSLCerts = trustAllSSLCerts;
        setup();
    }

    /**
     * Setup the client and needed SSL configuration
     */
    @PostConstruct
    public void setup() {
        this.shopBaseUrl = shopUrl + BASE_URL_PATH;

        ApacheHttpClientConfig clientConfig = new DefaultApacheHttpClientConfig();
        clientConfig.getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

        this.client = ApacheHttpClient.create(clientConfig);
        this.authorizationResource = addStandardQueryParameters(this.client.resource(this.shopBaseUrl.replace("http", "https") + "/customers/auth"));
        this.basketResource = addStandardQueryParameters(this.client.resource(this.shopBaseUrl.replace("http", "https") + "/baskets"));
        this.productResource = addStandardQueryParameters(this.client.resource(this.shopBaseUrl + "/products"));
        this.productSearchResource = addStandardQueryParameters(this.client.resource(this.shopBaseUrl + "/product_search"));
        this.categoryResource = addStandardQueryParameters(this.client.resource(this.shopBaseUrl + "/categories"));


        if ( this.trustAllSSLCerts ) {
            // SSL configuration
            clientConfig.getProperties().put(com.sun.jersey.client.urlconnection.HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), getNonValidatingSecurityContext()));
        }
    }

    /**
     * Add standard query parameters that normally are included in requests towards Demandware
     *
     * @param webResource
     * @return resource
     */
    private WebResource addStandardQueryParameters(WebResource webResource) {
        webResource = webResource.queryParam("client_id", this.clientId);
        if ( this.locale != null && !this.locale.isEmpty() ) {
            webResource = webResource.queryParam("locale", this.locale);
        }
        if ( this.currency != null && !this.currency.isEmpty() ) {
            webResource = webResource.queryParam("currency", this.currency);
        }
        return webResource;
    }

    /**
     * Get a non validating SSL security context. Should not be used in production.
     * @return SSL context
     */
    private static SSLContext getNonValidatingSecurityContext() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            return sc;
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Get hostname verifier for an unsecure SSL setup (used in test environments)
     * @return verifier
     */
    private static HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        };
    }

    /**
     * Authorize as guest
     * @return authorization header
     */
    private String authorizeAsGuest() {
        ClientResponse response =
                new Builder(this.authorizationResource.getRequestBuilder()).
                jsonHeaders().
                originHeader().getRequestBuilder().
                post(ClientResponse.class, new AuthenticationRequest(AuthenticationRequest.GUEST));
        return response.getHeaders().getFirst("Authorization");
    }

    /**
     * Throw exception based on fault message.
     * @param fault
     * @throws ECommerceException
     */
    private void throwException(Fault fault) throws ECommerceException  {
        throw new ECommerceException(fault.getType() + ": " + fault.getMessage());
    }

    /**
     * Get basket
     * @param clientResponse
     * @param authToken
     * @return
     * @throws ECommerceException
     */
    private Basket getBasket(ClientResponse clientResponse, String authToken) throws ECommerceException {

        Basket basket = clientResponse.getEntity(Basket.class);
        if ( basket.getFault() != null ) {
            this.throwException(basket.getFault());
        }
        basket.setAuthorizationToken(authToken);
        basket.setEtag(clientResponse.getHeaders().getFirst("ETag"));
        return basket;
    }

    /**
     * Create new basket
     * @return basket
     * @throws ECommerceException
     */
    public Basket createBasket() throws ECommerceException {
        // TODO: Support other authorization patterns here as well
        //
        String authToken = this.authorizeAsGuest();
        ClientResponse response =
                new Builder(this.basketResource.getRequestBuilder()).
                jsonHeaders().
                originHeader().getRequestBuilder().
                header("Authorization", authToken).
                post(ClientResponse.class, new Basket());
        return getBasket(response, authToken);
    }

    /**
     * Get basket
     * @param id
     * @return
     * @throws ECommerceException
     */
    public Basket getBasket(String id, String authenticationToken) throws ECommerceException {
        ClientResponse response =
                new Builder(this.basketResource.path(id).getRequestBuilder()).
                        jsonHeaders().
                        originHeader().
                        getRequestBuilder().
                        header("Authorization", authenticationToken).
                        get(ClientResponse.class);
        return getBasket(response, authenticationToken);
    }

    /**
     * Add a product to the basket
     * @param basket
     * @param productId
     * @param quantity
     * @return basket
     * @throws ECommerceException
     */
    public Basket addProductToBasket(Basket basket, String productId, int quantity) throws ECommerceException {

        // Check if the product is already added to the cart -> if so modify the cart item
        //
        if ( basket.getProduct_items() != null ) {
            for (ProductItem productItem : basket.getProduct_items()) {
                if ( productItem.getProduct_id().equals(productId) ) {
                    productItem.setQuantity(productItem.getQuantity()+quantity);
                    return this.modifyProductItemInBasket(basket, productItem);
                }
            }
        }

        // Create a new cart item
        //
        ProductItem productItem = new ProductItem();
        productItem.setProduct_id(productId);
        productItem.setQuantity(quantity);

        return this.addProductItemToBasket(basket, productItem);
    }

    /**
     * Add item to the basket.
     * @param basket
     * @param productItem
     * @return basket
     * @throws ECommerceException
     */
    public Basket addProductItemToBasket(Basket basket, ProductItem productItem) throws ECommerceException {
        ClientResponse response =
                new Builder(this.basketResource.path(basket.getBasket_id() + "/items").getRequestBuilder()).
                jsonHeaders().
                originHeader().
                basketHeaders(basket).getRequestBuilder().
                post(ClientResponse.class, productItem);
        return this.getBasket(response, basket.getAuthorizationToken());
    }

    /**
     * Modify item in basket.
     * @param basket
     * @param productItem
     * @return basket
     * @throws ECommerceException
     */
    public Basket modifyProductItemInBasket(Basket basket, ProductItem productItem) throws ECommerceException {
        ClientResponse response =
                new Builder(this.basketResource.path(basket.getBasket_id() + "/items/" + productItem.getItem_id()).getRequestBuilder()).
                jsonHeaders().
                originHeader().
                basketHeaders(basket).getRequestBuilder().
                method("PATCH", ClientResponse.class, productItem);
        return this.getBasket(response, basket.getAuthorizationToken());
    }

    /**
     * Remove item from basket
     * @param basket
     * @param productItem
     * @return basket
     * @throws ECommerceException
     */
    public Basket removeProductItemFromBasket(Basket basket, ProductItem productItem) throws ECommerceException {
        ClientResponse response =
                new Builder(this.basketResource.path(basket.getBasket_id() + "/items/" + productItem.getItem_id()).getRequestBuilder()).
                jsonHeaders().
                originHeader().
                basketHeaders(basket).getRequestBuilder().
                delete(ClientResponse.class);
        return this.getBasket(response, basket.getAuthorizationToken());
    }

    /**
     * Get product by ID.
     * @param productId
     * @return product
     */
    public Product getProduct(String productId) {
        Product product =
                this.productResource.path("/" + productId).
                queryParam("expand", "availability,links,options,images,prices,variations").
                accept(MediaType.APPLICATION_JSON).
                get(Product.class);
        if ( product.getFault() != null ) {
            this.throwException(product.getFault());
        }
        return product;
    }

    /**
     * Get category by ID.
     * @param categoryId
     * @return category
     */
    public Category getCategory(String categoryId) {
        return this.categoryResource.path("/" + categoryId).
                queryParam("levels", "0").
                accept(MediaType.APPLICATION_JSON).
                get(Category.class);
    }

    /**
     * Get top level categories with defined level of child categories
     * @param levels
     * @return categories
     */
    public List<Category> getTopLevelCategories(int levels) {
        return this.getCategories("root", levels);
    }

    /**
     * Get categories belonging to specified category parent.
     * @param categoryId
     * @param levels can be 0-2
     * @return
     */
    public List<Category> getCategories(String categoryId, int levels) {
        Category category = this.categoryResource.path(categoryId).
                queryParam("levels", Integer.toString(levels)).
                accept(MediaType.APPLICATION_JSON).
                get(Category.class);
        return category.getCategories();
    }

    /**
     * Get whole category tree
     * @param categories
     */
    public void getCategoryTree(List<Category> categories) {
        for ( Category category : categories ) {
            if ( category.getCategories() == null ) {
                category.setCategories(this.getCategories(category.getId(), 1));
            }
            if ( category.getCategories() != null ) {
                getCategoryTree(category.getCategories());
            }
        }
    }


    // TODO: Use this API instead: /search_suggestion ????
    /**
     * Search products by category
     * @param categoryId
     * @param count
     * @return search result
     */
    public ProductSearchResult searchProductsByCategory(String categoryId, int count) {
        return this.productSearchResource.
                queryParam("refine_1", "cgid=" +categoryId).
                queryParam("count", Integer.toString(count)).
                accept(MediaType.APPLICATION_JSON).
                get(ProductSearchResult.class);

    }

    /**
     * Search products by category and refinements (facets)
     * @param categoryId
     * @param count
     * @param refinements
     * @return search result
     */
    public ProductSearchResult searchProductsByCategory(String categoryId, int count, Map<String,String> refinements ) {
        WebResource searchResource = this.productSearchResource.
                queryParam("refine_1", "cgid=" +categoryId);

        int refineId = 2;
        for ( String refinementId : refinements.keySet() ) {
            searchResource = searchResource.queryParam("refine_" + refineId, refinementId + "=" + refinements.get(refinementId));
            refineId++;
        }

        return searchResource.
                queryParam("count", Integer.toString(count)).
                accept(MediaType.APPLICATION_JSON).
                get(ProductSearchResult.class);

    }

    /**
     * Search products using specified category ID and/or search phrase.
     * @param searchPhrase
     * @param categoryId
     * @param start
     * @param count
     * @param refinements
     * @return search result
     */
    public ProductSearchResult search(String searchPhrase, String categoryId, int start, int count, Map<String,String> refinements) {
        WebResource searchResource = this.productSearchResource;
        if ( searchPhrase != null ) {
            // TODO: Need to do URL encode here? Does Jersey solves this for us????
            searchResource = searchResource.queryParam("q", searchPhrase); //URLEncoder.encode(searchPhrase));
        }
        if ( start != 0 ) {
            searchResource = searchResource.queryParam("start", Integer.toString(start));
        }

        int refineId = 1;
        if ( categoryId != null ) {
            searchResource = searchResource.queryParam("refine_1", "cgid=" +categoryId);
            refineId++;
        }

        if ( refinements != null ) {
            for ( String refinementId : refinements.keySet() ) {
                searchResource = searchResource.queryParam("refine_" + refineId, refinementId + "=" + refinements.get(refinementId));
                refineId++;
            }
        }

        return searchResource.
                queryParam("count", Integer.toString(count)).
                queryParam("expand", "availability,images,prices,variations").
                accept(MediaType.APPLICATION_JSON).
                get(ProductSearchResult.class);
    }

    /**
     * Get next result set.
     * @param searchResult
     * @return search result
     */
    public ProductSearchResult getNext(ProductSearchResult searchResult) {
        if ( searchResult.getNext() != null ) {
            return this.client.resource(searchResult.getNext()).
                    accept(MediaType.APPLICATION_JSON).
                    get(ProductSearchResult.class);
        }
        return null;
    }

    /**
     * Get previous result set.
     * @param searchResult
     * @return search result
     */
    public ProductSearchResult getPrevious(ProductSearchResult searchResult) {
        if (searchResult.getPrevious() != null) {
            return this.client.resource(searchResult.getPrevious()).
                    accept(MediaType.APPLICATION_JSON).
                    get(ProductSearchResult.class);
        }
        return null;
    }

}
