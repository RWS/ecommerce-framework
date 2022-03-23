using Newtonsoft.Json;
using RestSharp;
using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Service;
using SDL.ECommerce.Rest.Model;
using System;
using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.Rest.Service
{
    /// <summary>
    /// E-Commerce Product Detail Service
    /// </summary>
    public class ProductDetailService : IProductDetailService
    {
        private IRestClient restClient;
        private IProductCategoryService productCategoryService;
        private IECommerceCacheProvider cacheProvider;
        private string environment;

        public ProductDetailService(IRestClient restClient, IProductCategoryService productCategoryService, IECommerceCacheProvider cacheProvider, string environment)
        {
            this.restClient = restClient;
            this.productCategoryService = productCategoryService;
            this.cacheProvider = cacheProvider;
            this.environment = environment;
        }

        public IProduct GetDetail(string productId)
        {
            return GetDetail(productId, null);
        }

        public IProduct GetDetail(string productId, IDictionary<string, string> variantAttributes)
        {
            Product product;
            var cacheKey = GetCacheKey(productId, variantAttributes);

            if (!this.cacheProvider.TryGet(CacheRegion.ECommerceProductDetail, cacheKey, out product))
            {
                var request = new RestRequest("/product/" + productId, Method.GET);
                if (variantAttributes != null)
                {
                    foreach (var variantAttribute in variantAttributes)
                    {
                        request.AddParameter(variantAttribute.Key, variantAttribute.Value);
                    }
                }
                var response = this.restClient.Execute(request);
                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    product = JsonConvert.DeserializeObject<Product>(response.Content);
                    product.ProductCategoryService = productCategoryService;
                    this.cacheProvider.Store<IProduct>(CacheRegion.ECommerceProductDetail, cacheKey, product);
                }
                else if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
                {
                    return null;
                }
                else
                {
                    throw new Exception("Could not get product details. Error Code: " + response.StatusCode + ", Error Message: " + response.ErrorMessage);
                }            
            }
            return product;
        }

        private string GetCacheKey(string productId, IDictionary<string, string> variantAttributes)
        {
            string cacheKey = "";
            if (environment != null)
            {
                cacheKey = environment + ":";
            }
            if (variantAttributes != null)
            {
                cacheKey += productId + "#" + string.Join("#", variantAttributes.Select(attr => attr.Key + "=" + attr.Value));
            }
            else
            {
                cacheKey += productId;
            }
            return cacheKey;
        }
    }    
}
