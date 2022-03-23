using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SDL.ECommerce.Api;

namespace SDL.ECommerce.Rest.Model
{
    public class Location : ILocation
    {
        public CategoryRef CategoryRef { get; set; }
        public List<string> CategoryFacets { get; set; }
        public ProductRef ProductRef { get; set; }
        public string StaticUrl { get; set; }

        ICategoryRef ILocation.CategoryRef
        {
            get
            {
                return CategoryRef;
            }
        }

        IList<FacetParameter> ILocation.Facets
        {
            get
            {
                return CategoryFacets.Select(facet => FacetParameter.FromUrl(facet)).ToList();
            }
        }

        IProductRef ILocation.ProductRef
        {
            get
            {
                return ProductRef;
            }
        }   
    }
}
