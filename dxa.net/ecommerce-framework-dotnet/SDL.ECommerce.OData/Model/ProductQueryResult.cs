using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.OData
{
    public partial class ProductQueryResult : IProductQueryResult
    { 

        ICollection<IBreadcrumb> IProductQueryResult.Breadcrumbs
        {
            get
            {
                return this.Breadcrumbs.Cast<IBreadcrumb>().ToList();
            }
        }

        int IProductQueryResult.CurrentSet
        {
            get
            {
                return (int) this.CurrentSet;
            }
        }

        ICollection<IFacetGroup> IProductQueryResult.FacetGroups
        {
            get
            {
                return this.FacetGroups.Cast<IFacetGroup>().ToList();
            }
        }

        ICollection<IProduct> IProductQueryResult.Products
        {
            get
            {
                return this.Products.Cast<IProduct>().ToList();
            }
        }

        ICollection<IPromotion> IProductQueryResult.Promotions
        {
            get
            {
                return this.Promotions.Select(promo => promo.ToConcretePromotion()).ToList();
               
            }
        }

        ICollection<IQuerySuggestion> IProductQueryResult.QuerySuggestions
        {
            get
            {
                return this.QuerySuggestions.Cast<IQuerySuggestion>().ToList();
               
            }
        }

        ILocation IProductQueryResult.RedirectLocation
        {
            get
            {
                return this.RedirectLocation;
            }
        }

        int IProductQueryResult.StartIndex
        {
            get
            {
                return (int) this.StartIndex;
            }
        }

        int IProductQueryResult.TotalCount
        {
            get
            {
                return (int) this.TotalCount;
            }
        }

        int IProductQueryResult.ViewSize
        {
            get
            {
                return (int) this.ViewSize;
            }
        }
    }
}
