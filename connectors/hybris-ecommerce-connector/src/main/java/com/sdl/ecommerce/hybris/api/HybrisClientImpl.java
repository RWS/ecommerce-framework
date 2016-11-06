package com.sdl.ecommerce.hybris.api;

import com.sdl.ecommerce.api.ECommerceException;
import com.sdl.ecommerce.hybris.api.model.*;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.*;

/**
 * Hybris Client
 *
 * Implements the OCC v1 REST interface.
 * https://wiki.hybris.com/display/release5/v1
 *
 * @author nic
 */
public class HybrisClientImpl implements HybrisClient {

    private String hybrisRestUrl;
    private String username;
    private String password;
    private String activeServiceCatalog;
    // TODO: Proxy the product images
    private String mediaUrlPrefix;
    private String language;
    private String currency;
    private List<String> excludeFacetList = new ArrayList<>();

    /**
     * Create new Hybris API client.
     *
     * @param hybrisRestUrl
     * @param username
     * @param password
     * @param activeServiceCatalog
     * @param language
     * @param currency
     * @param mediaUrlPrefix
     * @param excludeFacetList
     */
    public HybrisClientImpl(String hybrisRestUrl,
                            String username,
                            String password,
                            String activeServiceCatalog,
                            String language,
                            String currency,
                            String mediaUrlPrefix,
                            List<String> excludeFacetList) {
        this.hybrisRestUrl = hybrisRestUrl;
        this.username = username;
        this.password = password;
        this.activeServiceCatalog = activeServiceCatalog;
        this.language = language;
        this.currency = currency;
        this.mediaUrlPrefix = mediaUrlPrefix;
        this.excludeFacetList = excludeFacetList;
    }

    private RestTemplate createRestTemplate() {
        RestTemplate restTemplate;
        if ( this.username != null ) {
            SimpleClientHttpRequestFactory requestFactory
                    = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                    super.prepareConnection(connection, httpMethod);

                    //Basic Authentication for Police API
                    String authorisation = username + ":" + password;
                    // This is only available in JDK8
                    //byte[] encodedAuthorisation = Base64.getEncoder().encode(authorisation.getBytes());
                    String encodedAuthorisation = DatatypeConverter.printBase64Binary(authorisation.getBytes());
                    connection.setRequestProperty("Authorization", "Basic " + encodedAuthorisation);
                }
            };
            restTemplate = new RestTemplate(requestFactory);
        }
        else {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    /**
     * Get URL fragment with locale specific options
     * @param prefix
     * @return
     */
    private String getLocaleSpecificOptions(String prefix) {
        StringBuilder options = new StringBuilder();
        if ( this.language != null && !this.language.isEmpty() ) {
            options.append(prefix);
            options.append("lang=");
            options.append(this.language);
        }
        if ( this.currency != null && !this.currency.isEmpty() ) {
            if ( options.length() > 0 )
            {
                options.append("&");
            }
            else {
                options.append(prefix);
            }
            options.append("curr=");
            options.append(this.currency);
        }
        return options.toString();
    }

    public List<Category> getAllCategories() {

        RestTemplate restTemplate = this.createRestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("serviceUrl", this.hybrisRestUrl + this.activeServiceCatalog);
        vars.put("options", "CATEGORIES,SUBCATEGORIES");

        CatalogVersion catalog = restTemplate.getForObject(
                "{serviceUrl}?options={options}" + this.getLocaleSpecificOptions("&"),
                CatalogVersion.class, vars);
        return catalog.getCategories();
    }

    public Category getCategory(String categoryId) {
        RestTemplate restTemplate = this.createRestTemplate();
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("categoryid", categoryId);
        vars.put("serviceUrl", this.hybrisRestUrl + this.activeServiceCatalog);
        vars.put("options", "PRODUCTS");

        Category category = restTemplate.getForObject(
                "{serviceUrl}/categories/{categoryid}?options={options}" + this.getLocaleSpecificOptions("&"),
                Category.class, vars);
        this.processImages(category.getProducts());
        return category;
    }

    public SearchResult search(String searchPhrase, int pageSize, int currentPage) {
        return this.search(searchPhrase, pageSize, currentPage, null, null);
    }

    public SearchResult search(String searchPhrase, int pageSize, int currentPage, String sort, List<FacetPair> facets) {

        RestTemplate restTemplate = this.createRestTemplate();

        if ( searchPhrase == null ) {
            searchPhrase = "";
        }
        if ( sort == null ) {
            sort = "relevance";
        }

        String query = URLEncoder.encode(searchPhrase) + ":" + sort;
        if ( facets != null && facets.size() > 0 ) {
            for ( FacetPair facet : facets) {
                query += ":" + facet.getId() + ":" + facet.getValue();
            }
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("serviceUrl", this.hybrisRestUrl);
        vars.put("query", query);
        vars.put("pageSize", pageSize);
        vars.put("currentPage", currentPage);

        SearchResult result = restTemplate.getForObject(
                "{serviceUrl}/products?query={query}&pageSize={pageSize}&currentPage={currentPage}" + this.getLocaleSpecificOptions("&"),
                SearchResult.class, vars);
        this.processImages(result.getProducts());
        this.processFacets(result.getFacets());
        return result;
    }

    public Product getProduct(String productId) {

        RestTemplate restTemplate = this.createRestTemplate();

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("productid", productId);
        vars.put("serviceUrl", this.hybrisRestUrl);
        vars.put("options",  "BASIC,DESCRIPTION,GALLERY,CATEGORIES,PROMOTIONS,STOCK,REFERENCES,PRICE");

        // Add CLASSIFICATION,REVIEW as option when displaying of features is needed

        // TODO: Is needed????
        /*
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
        MediaType mediaType = new MediaType("application", "json",
                Charset.forName("UTF-8"));
        supportedMediaTypes.add(mediaType);
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setSupportedMediaTypes(supportedMediaTypes);
        messageConverters.add(jacksonConverter);
        restTemplate.setMessageConverters(messageConverters);
         */


        Product product = restTemplate.getForObject(
                "{serviceUrl}/products/{productid}?options={options}" + this.getLocaleSpecificOptions("&"),
                Product.class, vars);
        this.processImages(product);
        return product;
    }

    private void processImages(List<Product> products) {
        for ( Product product : products ) {
            processImages(product);
        }
    }

    private void processImages(Product product) {
        for ( Image image : product.getImages() ) {
            String imageUrl = image.getUrl();
            imageUrl = this.mediaUrlPrefix + imageUrl;
            image.setUrl(imageUrl);
        }
    }

    private void processFacets(List<Facet> facets) {
        for ( String excludeFacetId : this.excludeFacetList ) {
            for ( int i=0; i < facets.size(); i++ ) {
                Facet facet = facets.get(i);
                if ( facet.getId().equals(excludeFacetId) ) {
                    facets.remove(facet);
                    break;
                }
            }
        }
    }

    private HttpEntity getHeaders(String sessionID)
    {
        HttpHeaders requestHeaders = new HttpHeaders();
        if(sessionID != null)
        {
            requestHeaders.add("Cookie", sessionID);
        }
        HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
        return requestEntity;
    }

    private ResponseEntity getCartResponse(String sessionID)
    {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity cartResponse = restTemplate.exchange(
                this.hybrisRestUrl + "/cart" + this.getLocaleSpecificOptions("?"),
                HttpMethod.GET,
                getHeaders(sessionID),
                Cart.class);
        return cartResponse;
    }

    public String createCart() {
        ResponseEntity cartResponse = getCartResponse(null);
        HttpHeaders responseHeaders = cartResponse.getHeaders();
        String sessionID = responseHeaders.get("Set-Cookie").get(0);
        return sessionID;
    }

    @SuppressWarnings("rawtypes")
    public Cart getCart(String sessionID) {
        ResponseEntity cartResponse = getCartResponse(sessionID);
        Cart cart = (Cart) cartResponse.getBody();
        return cart;
    }

    @SuppressWarnings("rawtypes")
    public Cart addItemToCart(String sessionID, String productId, int amount) throws ECommerceException {
        Cart c = getCart(sessionID);
        for(Entry productEntry : c.getEntries())
        {
            if(productEntry.getProduct().getCode().equals(productId))
            {
                return this.modifyItemInCart(sessionID, productId, productEntry.getQuantity()+amount);
            }
        }
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("productId", productId);
        vars.put("qty", String.valueOf(amount));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity cartResponse = restTemplate.exchange(
                this.hybrisRestUrl + "/cart/entry?code={productId}&qty={qty}",
                HttpMethod.POST,
                getHeaders(sessionID),
                String.class,
                vars);

        if(is2xxSuccessful(cartResponse.getStatusCode()))
        {
            return getCart(sessionID);
        }
        else
        {
            throw new ECommerceException(cartResponse.getStatusCode().toString());
        }
    }

    public Cart removeItemFromCart(String sessionID, String productId) throws ECommerceException {

        Cart c =(Cart) getCart(sessionID);
        int entryId = -1;
        for(Entry productEntry : c.getEntries())
        {
            if(productEntry.getProduct().getCode().equals(productId))
            {
                entryId= productEntry.getEntryNumber();
            }
        }

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("entryId", String.valueOf(entryId));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity cartResponse = restTemplate.exchange(
                this.hybrisRestUrl + "/cart/entry/{entryId}",
                HttpMethod.DELETE,
                getHeaders(sessionID),
                String.class,
                vars);
        if(is2xxSuccessful(cartResponse.getStatusCode()))
        {
            return getCart(sessionID);
        }
        else
        {
            throw new ECommerceException(cartResponse.getStatusCode().toString());
        }
    }

    public Cart modifyItemInCart(String sessionID, String productId,
                                   int amount) throws ECommerceException {

        Cart c = getCart(sessionID);
        int entryId = -1;
        for(Entry productEntry : c.getEntries())
        {
            if(productEntry.getProduct().getCode().equals(productId))
            {
                entryId= productEntry.getEntryNumber();
            }
        }

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("entryId", String.valueOf(entryId));
        vars.put("qty", String.valueOf(amount));

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity cartResponse = restTemplate.exchange(
                this.hybrisRestUrl + "/cart/entry/{entryId}?qty={qty}",
                HttpMethod.PUT,
                getHeaders(sessionID),
                String.class,
                vars);
        if(is2xxSuccessful(cartResponse.getStatusCode()))
        {
            return getCart(sessionID);
        }
        else
        {
            throw new ECommerceException(cartResponse.getStatusCode().toString());
        }
    }

    private boolean is2xxSuccessful(HttpStatus status) {
        return status.value() >= 200 && status.value() < 300;
    }



}
