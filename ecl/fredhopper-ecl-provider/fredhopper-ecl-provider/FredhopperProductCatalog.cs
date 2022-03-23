
using com.fredhopper.lang.query;
using com.fredhopper.lang.query.location;
using com.fredhopper.lang.query.location.criteria;
using SDL.ECommerce.Ecl;
using SDL.Fredhopper.Ecl.FredhopperWS;
using System;
using System.Linq;
using System.Collections.Generic;
using System.ServiceModel;
using System.Xml.Linq;
using System.ServiceModel.Channels;
using System.Net;
using System.Xml;

namespace SDL.Fredhopper.Ecl
{
    /// <summary>
    /// Fredhopper Product Catalog
    /// </summary>
    public class FredhopperProductCatalog : ProductCatalog
    {
        private FASWebServiceClient fhClient;
        private IDictionary<int, PublicationConfiguration> publicationConfigurations = new Dictionary<int, PublicationConfiguration>();

        static int DEFAULT_MAX_RECEIVED_MESSAGE_SIZE = 10485760;

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="configuration"></param>
        public FredhopperProductCatalog(XElement configuration)
        {
            var endpointAddressStr = configuration.Element(EclProvider.EcommerceEclNs + "EndpointAddress").Value;
            var usernameElement = configuration.Element(EclProvider.EcommerceEclNs + "UserName");
            var passwordElement = configuration.Element(EclProvider.EcommerceEclNs + "Password");
            var maxReceivedMessageSize = EclProvider.GetIntConfigurationValue(configuration, "MaxReceivedMessageSize", DEFAULT_MAX_RECEIVED_MESSAGE_SIZE);

            Binding binding;
            if (endpointAddressStr.StartsWith("https")) // Secure SSL connection
            {              
                ServicePointManager.SecurityProtocol =
                        SecurityProtocolType.Tls | SecurityProtocolType.Tls11 | SecurityProtocolType.Tls12;
                
                binding = new BasicHttpsBinding(BasicHttpsSecurityMode.Transport)
                {
                    MaxReceivedMessageSize = maxReceivedMessageSize
                };
                if (usernameElement != null)
                {
                    ((BasicHttpsBinding)binding).Security.Transport = new HttpTransportSecurity
                    {
                        ClientCredentialType = HttpClientCredentialType.Basic
                    };
                }
            }
            else // Unsecure HTTP connection
            {
                binding = new BasicHttpBinding(BasicHttpSecurityMode.TransportCredentialOnly)
                {
                    MaxReceivedMessageSize = maxReceivedMessageSize
                };
                if (usernameElement != null)
                {
                    ((BasicHttpBinding)binding).Security.Transport = new HttpTransportSecurity
                    {
                        ClientCredentialType = HttpClientCredentialType.Basic
                    };
                }
            }

            var endpointAddress = new EndpointAddress(endpointAddressStr);
            this.fhClient = new FASWebServiceClient(binding, endpointAddress);
            if ( usernameElement != null )
            {
                this.fhClient.ChannelFactory.Credentials.UserName.UserName = usernameElement.Value;
                this.fhClient.ChannelFactory.Credentials.UserName.Password = passwordElement.Value;
            }

            // TODO: How to handle creation of new publications through SiteWizard? This will require some reconfig for each publication...
            // Use a fallback for those cases or???

            XElement publicationConfigurations = configuration.Element(EclProvider.EcommerceEclNs + "PublicationConfigurations");
            foreach (var config in publicationConfigurations.Elements(EclProvider.EcommerceEclNs + "PublicationConfiguration") )
            {
                var publicationIds = config.Attribute("publicationIds").Value; // Format: 12,34,45,55
                var publicationConfig = new PublicationConfiguration();
                publicationConfig.Locale = config.Element(EclProvider.EcommerceEclNs + "Locale").Value;
                publicationConfig.Universe = config.Element(EclProvider.EcommerceEclNs + "Universe").Value;
                var fallback = config.Attribute("fallback");
                if ( fallback != null && fallback.Value.Equals("true") )
                {
                    publicationConfig.Fallback = true;
                }
                var modelMappings = config.Element(EclProvider.EcommerceEclNs + "ModelMappings").Value;
                var modelMappingsMap = new Dictionary<string, string>();
                var tokens = modelMappings.Split(new char[] { ' ', ';', '=' }, StringSplitOptions.RemoveEmptyEntries);
                int i = 0;
                while ( i < tokens.Length )
                {
                    var name = tokens[i];
                    var value = tokens[i + 1];
                    modelMappingsMap.Add(name, value);
                    i = i + 2;
                }
                publicationConfig.ModelMappings = modelMappingsMap;

                publicationConfig.Filters = new Dictionary<string, string>();
                var filters = config.Element(EclProvider.EcommerceEclNs + "Filters");
                if (filters != null)
                {
                    foreach (var filter in filters.Elements(EclProvider.EcommerceEclNs + "Filter"))
                    {
                        var filterName = filter.Attribute("name").Value;
                        var filterValue = filter.Attribute("value").Value;
                        publicationConfig.Filters.Add(filterName, filterValue);
                    }
                }
                var ids = publicationIds.Split(new char[] { ' ', ',' }, StringSplitOptions.RemoveEmptyEntries);
                foreach ( var id in ids )
                {
                    this.publicationConfigurations.Add(Int32.Parse(id), publicationConfig);
                }
            }
        }

        public Category GetAllCategories(int publicationId)
        {
           return this.GetRootCategory(publicationId);
        }

        public Category GetCategory(string categoryId, int publicationId)
        {
            if (categoryId == null)
            {
                return this.GetRootCategory(publicationId);
            }

            Category parentCategory;
            if (!categoryId.Contains("_") || (categoryId.Count(c => c == '_') == 1))
            {
                // Top level category
                //
                parentCategory = this.GetRootCategory(publicationId);
            }
            else
            {
                var parentId = categoryId.Substring(0, categoryId.LastIndexOf("_"));
                parentCategory = this.GetCategory(categoryId, this.GetRootCategory(publicationId));
            }

            foreach (var category in parentCategory.Categories)
            {
                if (category.CategoryId != null && category.CategoryId.Equals(categoryId))
                {
                    return category;
                }
            }
            return null;
        }

        private Category GetCategory(string categoryId, Category category)
        {
            if ( category == null || category.Categories == null )
            {
                return null;
            }
            foreach ( var subCategory in category.Categories)
            {
                if ( subCategory.CategoryId.Equals(categoryId) )
                {
                    return subCategory;
                }
                else
                {
                    Category matchingCategory = this.GetCategory(categoryId, subCategory);
                    if ( matchingCategory != null )
                    {
                        return matchingCategory;
                    }
                }
            }
            return null;
        }

        public QueryResult GetCategoryAndProducts(string categoryId, int publicationId, int pageIndex)
        {
            // TODO: What products to show? Should we only show leaf products?
            // TODO: Have different configurable strategies here??

            Query query = new Query(GetLocation(publicationId, categoryId));
            query.setListStartIndex(pageIndex * EclProvider.ProductPageSize);
            return QueryProducts(query, categoryId, publicationId);
        }

        public QueryResult Search(string searchTerm, string categoryId, int publicationId, int pageIndex)
        {
            Location location = categoryId != null ? GetLocation(publicationId, categoryId) : GetLocation(publicationId);
            location.addCriterion(new SearchCriterion(searchTerm));
            Query query = new Query(location);
            query.setListStartIndex(pageIndex); // TODO: This is probably a bug: this is not page index we are using towards Fredhopper!!!
            return QueryProducts(query, categoryId, publicationId);
        }

        public Product GetProduct(string id, int publicationId)
        {
            var location = GetLocation(publicationId);
            Query query = new Query(location);
            query.addSecondId(id);
            query.setView(ViewType.DETAIL);
            page fhPage = this.fhClient.getAll(query.toString());
            universe universe = this.GetUniverse(fhPage);
            var modelMappings = this.GetPublicationConfiguration(publicationId).ModelMappings;
            var itemsSection = universe.itemssection;
            if (universe.itemssection != null)
            {
                foreach (var item in universe.itemssection.items)
                {
                    return new FredhopperProduct(item, modelMappings);
                }
            }
            return null;
        }

        internal void LoadCategories(FredhopperCategory parentCategory, IList<Category> currentCategories)
        {
            Location location;
            int publicationId = parentCategory.PublicationId;
            if (parentCategory != null && parentCategory.CategoryId != null)
            {
                location = GetLocation(publicationId, parentCategory.CategoryId);
            }
            else
            {
                location = GetLocation(publicationId);
            }
            Query query = new Query(location);
            page fhPage = this.fhClient.getAll(query.toString());

            universe universe = this.GetUniverse(fhPage);

            var facetmap = universe.facetmap[0];
            var filters = facetmap.filter ?? new filter[]{};

            IList<Category> newCategoryList = new List<Category>();

            var level = parentCategory != null && parentCategory.CategoryId != null ? GetCategoryLevel(parentCategory.CategoryId) : 0;

            foreach (var filter in filters)
            {
                if (filter.basetype == attributeTypeFormat.cat)
                {
                    foreach (var section in filter.filtersection)
                    {         
                                      
                        if (GetCategoryLevel(section.value.Value) <= level)
                        {
                            // Parent category -> pick next
                            //
                            continue;
                        }
                        var category = new FredhopperCategory(section.value.Value, section.link.name, parentCategory, publicationId, this);
                        var existingCategory = currentCategories?.FirstOrDefault(c => c.CategoryId.Equals(category.CategoryId));
                        if (existingCategory != null)
                        {
                            // Keep the old category with its sub-categories
                            //
                            newCategoryList.Add(existingCategory);
                        }
                        else
                        {
                            // Add new category
                            //
                            newCategoryList.Add(category);
                        }                        
                    }
                }
            }
            parentCategory.Categories = newCategoryList;
        }

        private int GetCategoryLevel(string categoryId)
        {
            return categoryId.Count(c => c == '_');
        }

        private IList<Category> GetCategories(universe universe, string parentCategoryId, int publicationId)
        {
            var facetmap = universe.facetmap[0];
            var filters = facetmap.filter;
            if (filters == null) // No sub-categories avaiable
            {
                return Enumerable.Empty<Category>().ToList();
            }
            var level = parentCategoryId != null ? GetCategoryLevel(parentCategoryId) : 0;

            IList<Category> categories = new List<Category>();

            foreach (var filter in filters)
            {
                if (filter.basetype == attributeTypeFormat.cat)
                {
                    foreach (var section in filter.filtersection)
                    {
                        if (GetCategoryLevel(section.value.Value) <= level)
                        {
                            // Parent category -> pick next
                            //
                            continue;
                        }
                       
                        var category = new FredhopperCategory(section.value.Value, section.link.name, null, publicationId, this);
                        categories.Add(category);
                    }
                }
            }
            return categories;
        }

        private Category GetRootCategory(int publicationId)
        {
            var publicationConfiguration = this.GetPublicationConfiguration(publicationId);        
            var rootCategory = new FredhopperCategory(null, null, null, publicationId, this);     
            return rootCategory;
        }

        private Location GetLocation(int publicationId)
        {
            var publicationConfig = this.GetPublicationConfiguration(publicationId);
            if ( publicationConfig == null )
            {
                // TODO: Throw an exception here
                return null;
            }
            return new Location("//" + publicationConfig.Universe + "/" + publicationConfig.Locale);
        }

        private Location GetLocation(int publicationId, string categoryId)
        {
            var location = GetLocation(publicationId);
            location.addCriterion(new CategoryCriterion("categories", categoryId));
            return location;
        }

        private universe GetUniverse(page fhPage)
        {
            foreach (var universe in fhPage.universes)
            {
                if (universe.type == universeType.selected)
                {
                    return universe;

                }
            }
            return null;
        }

        private QueryResult QueryProducts(Query query, string categoryId, int publicationId)
        {
            var config = this.GetPublicationConfiguration(publicationId);

            query.setThemesDisabled(true);
            query.setListViewSize(EclProvider.ProductPageSize);
            var isSearch = query.getLocation().getSearchCriterion() != null;
            foreach ( var filter in config.Filters )
            {
                query.getLocation().addCriterion(new SingleValuedCriterion(filter.Key, filter.Value));
            }
            page fhPage = this.fhClient.getAll(query.toString());
            universe universe = this.GetUniverse(fhPage);
            var modelMappings = config.ModelMappings;

            var result = new QueryResult();
            result.Products = new List<Product>();
            if (!isSearch)
            {
                // Extract categories from the FH result set
                //
                result.Categories = GetCategories(universe, categoryId, publicationId);
            }
            var itemsSection = universe.itemssection;
            if (universe.itemssection != null)
            {
                result.Total = universe.itemssection.results.total_items;
                result.NumberOfPages = universe.itemssection.results.total_items/universe.itemssection.results.view_size;
                if ( universe.itemssection.results.total_items % universe.itemssection.results.view_size != 0)
                {
                    result.NumberOfPages++;
                }
                foreach (var item in universe.itemssection.items)
                {
                    result.Products.Add(new FredhopperProduct(item, modelMappings));
                }
            }
            return result;
        }



        private PublicationConfiguration GetPublicationConfiguration(int publicationId)
        {
            if ( this.publicationConfigurations.ContainsKey(publicationId) )
            {
                return this.publicationConfigurations[publicationId];
            }
            else
            {
                // Use fallback
                //
                foreach ( var publicationConfig in this.publicationConfigurations.Values )
                {
                    if ( publicationConfig.Fallback )
                    {
                        return publicationConfig;
                    }
                }
            }
            return null; // TODO: Throw an exception here?
        }

    }

    class PublicationConfiguration
    {
        // TODO: Make a generic abstraction for this
        public string Universe { get; set; }
        public string Locale { get; set; }
        public IDictionary<string,string> ModelMappings { get; set; }
        public bool Fallback { get; set;  }
        public IDictionary<string, string> Filters { get; set; }
    }
}
