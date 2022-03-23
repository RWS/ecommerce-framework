using Sdl.Web.Delivery.Service;
using SDL.ECommerce.Api.Model;
using SDL.ECommerce.Api.Service;

using System.Linq;
using System.Text;

namespace SDL.ECommerce.OData
{
    /// <summary>
    /// E-Commerce Product Query Service
    /// </summary>
    public class ProductQueryService : IProductQueryService
    {
        private ECommerceClient ecommerceClient;

        /// <summary>
        /// Constructor (only available internally)
        /// </summary>
        /// <param name="service"></param>
        internal ProductQueryService(ECommerceClient ecommerceClient)
        {
            this.ecommerceClient = ecommerceClient;
        }

        /// <summary>
        /// Execute query towards the product catalog.
        /// </summary>
        /// <param name="query"></param>
        /// <returns></returns>
        public IProductQueryResult Query(Query query)
        {
            ODataV4ClientFunction func = new ODataV4ClientFunction("ProductQuery");
            func.AllowCaching = false;
           
            // Category
            //
            if ( query.Category != null )
            {
                func.WithParam("categoryId", query.Category.Id);
            }
            else if ( query.CategoryId != null )
            {
                func.WithParam("categoryId", query.CategoryId);
            }

            // Search phrase
            //
            if (query.SearchPhrase != null)
            {
                func.WithParam("searchPhrase", query.SearchPhrase);
            }

            // Facets
            //
            if ( query.Facets != null && query.Facets.Count > 0 )
            {
                var facetsString = new StringBuilder();
                for (int i = 0; i < query.Facets.Count; i++)
                {
                    var facet = query.Facets[i];
                    facetsString.Append(facet.ToUrl());
                    if (i + 1 < query.Facets.Count )
                    {
                        facetsString.Append("&");
                    }
                }
                func.WithParam("facets", facetsString.ToString());
            }
            if (query.StartIndex != null)
            {
                func.WithParam("startIndex", query.StartIndex);
            }
            if ( query.ViewSize != null)
            {
                func.WithParam("viewSize", query.ViewSize);
            }
            if ( query.ViewType != null )
            {
                func.WithParam("viewType", query.ViewType.ToString());
            }

            return Enumerable.FirstOrDefault<ProductQueryResult>(this.ecommerceClient.ODataV4Service.Execute<ProductQueryResult>(func));

            /*
            var uriString = string.Format("{0}/{1}", (object)this.service.Service.BaseUri, (object)func);
            Uri uri = new Uri(uriString);
            return Enumerable.FirstOrDefault<ProductQueryResult>(this.service.Service.Execute<ProductQueryResult>(uri));
            */

        }
    }
}
