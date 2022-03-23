using SDL.ECommerce.Api.Service;
using System;
using System.Text;
using SDL.ECommerce.Api.Model;
using RestSharp;
using SDL.ECommerce.Rest.Model;
using Newtonsoft.Json;
using SDL.ECommerce.Api;
using System.Linq;

namespace SDL.ECommerce.Rest.Service
{
    /// <summary>
    /// Product Query Service
    /// </summary>
    public class ProductQueryService : IProductQueryService
    {

        private IRestClient restClient;
        private IECommerceCacheProvider cacheProvider;
        private string environment;

        public ProductQueryService(IRestClient restClient, IECommerceCacheProvider cacheProvider, string environment)
        {
            this.restClient = restClient;
            this.cacheProvider = cacheProvider;
            this.environment = environment;
        }

        public IProductQueryResult Query(Query query)
        {

            // TODO: Add environment to the cache key

            var request = new RestRequest("/product/query", Method.GET);
            string cacheKey = "";
            CacheRegion cacheRegion = CacheRegion.ECommerceProductLister;

            // Environment
            //
            if (environment != null)
            {
                cacheKey += environment + ":";
            }

            // Category
            //
            if (query.Category != null)
            {
                request.AddParameter("categoryId", query.Category.Id);
                cacheKey += query.Category.Id;
            }
            else if (query.CategoryId != null)
            {
                request.AddParameter("categoryId", query.CategoryId);
                cacheKey += query.CategoryId;
            }
            else if (query.Categories != null)
            {
                var categoryOrString = string.Join("|", query.Categories.Select(c => c.Id));
                request.AddParameter("categoryIds", categoryOrString);
                cacheKey += categoryOrString;
            }
            else if (query.CategoryIds != null)
            {
                var categoryOrString = string.Join("|", query.CategoryIds);
                request.AddParameter("categoryIds", categoryOrString);
                cacheKey += categoryOrString;
            }

            // Search phrase
            //
            if (query.SearchPhrase != null)
            {
                request.AddParameter("searchPhrase", query.SearchPhrase);
                cacheKey += ":" + query.SearchPhrase;
                cacheRegion = CacheRegion.ECommerceSearch;
            }

            // Facets
            //
            if (query.Facets != null && query.Facets.Count > 0)
            {
                var facetsString = new StringBuilder();
                for (int i = 0; i < query.Facets.Count; i++)
                {
                    var facet = query.Facets[i];
                    facetsString.Append(facet.ToUrl());
                    if (i+1 < query.Facets.Count)
                    {
                        facetsString.Append("&");
                    }
                }
                request.AddParameter("facets", facetsString.ToString());
                cacheKey += "#" + facetsString.ToString();
            }
            if (query.StartIndex != null)
            {
                request.AddParameter("startIndex", query.StartIndex);
                cacheKey += "#" + query.StartIndex;
            }
            if (query.ViewSize != null)
            {
                request.AddParameter("viewSize", query.ViewSize);
                cacheKey += "@" + query.ViewSize;
            }
            if (query.ViewType != null)
            {
                request.AddParameter("viewType", query.ViewType.ToString());
                if ( query.ViewType == ViewType.FLYOUT )
                {
                    cacheRegion = CacheRegion.ECommerceFlyout;
                }          
            }
            // Context Data
            //
            if (query.ContextData != null && query.ContextData.Count > 0)
            {
                var contextDataString = new StringBuilder();
                int i = 0;
                foreach (var contextData in query.ContextData)
                {
                    contextDataString.Append(contextData.Key);
                    contextDataString.Append("=");
                    contextDataString.Append(contextData.Value);
                    if (i+1 < query.ContextData.Count)
                    {
                        contextDataString.Append("&");
                    }
                    i++;
                }
                request.AddParameter("contextData", contextDataString.ToString());
                cacheKey += "%" + contextDataString.ToString();
            }
            
            IProductQueryResult queryResult;
            if (!cacheProvider.TryGet(cacheRegion, cacheKey, out queryResult))
            {
                // TODO: Add Async I/O here !!!!
                var response = this.restClient.Execute(request);
                if (response.StatusCode == System.Net.HttpStatusCode.OK)
                {
                    queryResult = JsonConvert.DeserializeObject<ProductQueryResult>(response.Content);
                    cacheProvider.Store<IProductQueryResult>(cacheRegion, cacheKey, queryResult);
                }
                else
                {
                    var errorMessage = JsonConvert.DeserializeObject<ErrorMessage>(response.Content);
                    throw new Exception("Failure when doing product query. Error Code: " + response.StatusCode + ", Error Message: " + errorMessage?.Message);
                }
            }
            return queryResult;
        }

    }
}
