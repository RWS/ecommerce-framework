using System;
using SDL.Demandware.Ecl;
using SDL.ECommerce.Ecl;
using System.Collections.Generic;
using System.Xml.Linq;

namespace SDL.DemandWare.Ecl
{
    /// <summary>
    /// Demandware Product Catalog. 
    /// </summary>
    public class DemandWareProductCatalog : ProductCatalog
    {
        private ShopClient shopClient;

        public DemandWareProductCatalog(XElement configuration)
        {           
            this.shopClient = new ShopClient(configuration.Element(EclProvider.EcommerceEclNs + "ShopUrl").Value,
                                             configuration.Element(EclProvider.EcommerceEclNs + "ClientId").Value);
        }

        public DemandWareProductCatalog(string shopUrl, string clientId)
        {
            this.shopClient = new ShopClient(shopUrl, clientId);
        }

        public Category GetAllCategories(int publicationId)
        {
            return (Category) this.shopClient.GetAllCategories();
        }

        public Category GetCategory(string id, int publicationId)
        {
            return (Category) this.shopClient.GetCategory(id);
        }

        public QueryResult GetProducts(string categoryId, int publicationId, int pageIndex)
        {
            List<DemandWareProduct> list = (List<DemandWareProduct>) this.shopClient.GetProducts(categoryId, 100);
            var result = new QueryResult();
            result.NumberOfPages = 1; // TODO: Add support for pagination here
            result.Products = list.ConvertAll(x => (Product)x);
            return result;
        }

        public QueryResult Search(string searchTerm, string categoryId, int publicationId, int pageIndex)
        {
            List<DemandWareProduct> list = (List<DemandWareProduct>)this.shopClient.Search(searchTerm, categoryId, 100);
            var result = new QueryResult();
            result.NumberOfPages = 1; // TODO: Add support for pagination here
            result.Products = list.ConvertAll(x => (Product)x);
            return result;
        }

        public Product GetProduct(string id, int publicationId)
        {
            return (Product) this.shopClient.GetProduct(id);
        }
    }
}
