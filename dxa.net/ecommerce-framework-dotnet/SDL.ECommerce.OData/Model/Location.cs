using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

using SDL.ECommerce.Api;

namespace SDL.ECommerce.OData
{
    public partial class Location : ILocation
    {
        private IList<FacetParameter> facets;
          
        ICategoryRef ILocation.CategoryRef
        {
            get
            {
                return this.CategoryRef;
            }
        }

        IList<FacetParameter> ILocation.Facets
        {
            get
            {
                if ( this.Facets != null && facets == null )
                {
                    facets = this.Facets.Select(facet => FacetParameter.FromUrl(facet)).ToList();
                }
                return facets;
            }
        }

        IProductRef ILocation.ProductRef
        {
            get 
            {
                return this.ProductRef;
            }
        }
    
    }
}
