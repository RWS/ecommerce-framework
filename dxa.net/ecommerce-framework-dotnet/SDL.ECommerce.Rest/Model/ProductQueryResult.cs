using Newtonsoft.Json;
using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    class ProductQueryResult : IProductQueryResult
    {
        public List<Product> Products { get; set; }
        public List<FacetGroup> FacetGroups { get; set; }
        public List<Promotion> Promotions { get; set; }
        public List<Breadcrumb> Breadcrumbs { get; set; }
        public List<QuerySuggestion> QuerySuggestions { get; set; }
        public Location RedirectLocation { get; set; }
        public int StartIndex { get; set; }
        public int TotalCount { get; set; }
        public int ViewSize { get; set; }
        public int CurrentSet { get; set; }

        ICollection<IBreadcrumb> IProductQueryResult.Breadcrumbs
        {
            get
            {
                return Breadcrumbs?.Cast<IBreadcrumb>().ToList();
            }
        }

        ICollection<IFacetGroup> IProductQueryResult.FacetGroups
        {
            get
            {
                return FacetGroups?.Cast<IFacetGroup>().ToList();
            }
        }

        ICollection<IProduct> IProductQueryResult.Products
        {
            get
            {
                return Products?.Cast<IProduct>().ToList();
            }
        }

        ICollection<IPromotion> IProductQueryResult.Promotions
        {
            get
            {
                return Promotions?.Select(p => p.ToConcretePromotion()).ToList();
            }
        }

        ICollection<IQuerySuggestion> IProductQueryResult.QuerySuggestions
        {
            get
            {
                return QuerySuggestions?.Cast<IQuerySuggestion>().ToList();
            }
        }

        ILocation IProductQueryResult.RedirectLocation
        {
            get
            {
                return RedirectLocation;
            }
        }

    }

}
