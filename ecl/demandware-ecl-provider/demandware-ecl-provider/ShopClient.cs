using Newtonsoft.Json;
using SDL.DemandWare.Ecl;
using SDL.ECommerce.Ecl;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;

namespace SDL.Demandware.Ecl
{
    [Serializable]
    public class DemandWareException : Exception
    {
        public DemandWareException(String message, Exception cause) : base(message, cause) { }
    }

    /// <summary>
    /// Client to the Demandware Shop v16.1 REST interface.
    /// </summary>
    class ShopClient
    {
        const string BASE_URL_PATH = "/dw/shop/v16_1";
        const string CLIENT_ID_PARAM = "?client_id=";

        private String shopUrl;
        private String clientId;
        private String dwBaseUrl;
        private String clientIdRequestParam;

        public ShopClient(String shopUrl, String clientId)
        {
            this.shopUrl = shopUrl;
            this.clientId = clientId;
            this.dwBaseUrl = shopUrl + BASE_URL_PATH;
            this.clientIdRequestParam = CLIENT_ID_PARAM + clientId;
        }

        public DemandWareCategory GetAllCategories()
        {
            String jsonResponse = JsonGetRequest(this.dwBaseUrl + "/categories/root" + clientIdRequestParam + "&levels=1");
            DemandWareCategory rootCategory = JsonConvert.DeserializeObject<DemandWareCategory>(jsonResponse);
            GetCategoryTree(rootCategory.categories);
            return rootCategory;
        }

        protected void GetCategoryTree(IList<DemandWareCategory> categories, Category parent = null)
        {
            foreach ( DemandWareCategory category in categories ) 
            {
                category.parent = parent;
                category.categories = GetCategories(category.id);
                if ( category.categories != null )
                {
                    GetCategoryTree(category.categories, category);
                }
                else
                {
                    category.categories = new List<DemandWareCategory>();
                }
            }
        }

        public IList<DemandWareCategory> GetCategories(String categoryId)
        {
            String jsonResponse = JsonGetRequest(this.dwBaseUrl + "/categories/" + categoryId + clientIdRequestParam + "&levels=1");
            DemandWareCategory parentCategory = JsonConvert.DeserializeObject<DemandWareCategory>(jsonResponse);
            if (parentCategory == null || parentCategory.categories == null)
            {
                return null;
            }
            return parentCategory.categories;
        }

        public DemandWareCategory GetCategory(String categoryId)
        {
            String jsonResponse = JsonGetRequest(this.dwBaseUrl + "/categories/" + categoryId + clientIdRequestParam + "&levels=0");
            return JsonConvert.DeserializeObject<DemandWareCategory>(jsonResponse);
        }

        public IList<string> GetProductIds(String categoryId, int count)
        {
            String jsonResponse = JsonGetRequest(this.dwBaseUrl + "/product_search" + clientIdRequestParam +
                "&refine_1=cgid%3D" + categoryId + "&count=" + count);

            ProductSearchResult result = JsonConvert.DeserializeObject<ProductSearchResult>(jsonResponse);
            var products = result.hits;
            var productIds = new List<string>();
            foreach (var product in products)
            {
                productIds.Add(product.product_id);
            }
            return productIds;
        }

        public IList<DemandWareProduct> GetProducts(String categoryId, int count)
        {        
            string jsonResponse = JsonGetRequest(this.dwBaseUrl + "/product_search/images" + clientIdRequestParam +
                "&refine_1=cgid%3D" + categoryId + "&count=" + count);

            ProductSearchResult result = JsonConvert.DeserializeObject<ProductSearchResult>(jsonResponse);
            var products = result.hits;
            foreach (var product in products)
            {
                product.category_id = categoryId;
            }
            return products;            
        }

        public IList<DemandWareProduct> Search(string searchPhrase, String categoryId, int count)
        {
            var refinements = "";
            if (categoryId != null)
            {
                refinements = "&refine_1=cgid%3D" + categoryId;
            }
            string jsonResponse = JsonGetRequest(this.dwBaseUrl + "/product_search/images" + clientIdRequestParam +
                "&q=" + searchPhrase + refinements + "&count=" + count);
           
            ProductSearchResult result = JsonConvert.DeserializeObject<ProductSearchResult>(jsonResponse);
            var products = result.hits;
            return products;
        }

        public DemandWareProduct GetProduct(String productId)
        {
            try
            {
                String jsonResponse = JsonGetRequest(this.dwBaseUrl + "/products/" + productId +
                                        clientIdRequestParam + "&expand=availability,links,options,images,prices,variations");
                ProductDetail productDetail = JsonConvert.DeserializeObject<ProductDetail>(jsonResponse);
                return new DemandWareProduct(productDetail);
            }
            catch (Exception e)
            {
                throw new Exception("Could not get product with ID: " + productId, e);
            }
        }

        private string JsonGetRequest(string url)
        {
            string jsonResponse = string.Empty;

            var webRequest = System.Net.WebRequest.Create(url) as HttpWebRequest;
            if (webRequest != null)
            {
                webRequest.Timeout = 10000; // TODO: Have the timeout configurable.
                HttpWebResponse resp = (HttpWebResponse)webRequest.GetResponse();
                Stream resStream = resp.GetResponseStream();
                StreamReader reader = new StreamReader(resStream);
                jsonResponse = reader.ReadToEnd();
            }
            return jsonResponse;
        }

        private string JsonPostRequest(string url, string json)
        {
            string jsonResponse = string.Empty;
            StreamWriter requestWriter;

            var webRequest = System.Net.WebRequest.Create(url) as HttpWebRequest;
            if (webRequest != null)
            {
                webRequest.Method = "POST";
                webRequest.ServicePoint.Expect100Continue = false;
                webRequest.Timeout = 10000; // TODO: Have the timeout configurable. 
                webRequest.ContentType = "application/json; charset=utf-8";
                //POST the data.
                using (requestWriter = new StreamWriter(webRequest.GetRequestStream()))
                {
                    requestWriter.Write(json);
                }
            }
            HttpWebResponse resp = (HttpWebResponse)webRequest.GetResponse();
            Stream resStream = resp.GetResponseStream();
            StreamReader reader = new StreamReader(resStream);
            jsonResponse = reader.ReadToEnd();
            return jsonResponse;
        }
    }
}
