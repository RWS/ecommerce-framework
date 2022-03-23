using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;

namespace SDL.ECommerce.OData
{
    public partial class Facet : IFacet
    {

        int IFacet.Count
        {
            get
            {
                return (int) this.Count;
            }
        }

        bool IFacet.IsCategory
        {
            get
            {
                return (bool) this.IsCategory;
            }
        }

        bool IFacet.IsSelected
        {
            get
            {
                return (bool)this.IsSelected;
            }
        }

        FacetType IFacet.Type
        {
            get
            {
                return (FacetType) Enum.Parse(typeof(FacetType), this.Type);
            }
        }

        public string Value
        {
            get
            {
                if ( this.Values != null && this.Values.Count > 0 )
                {
                    return this.Values[0];
                }
                return null;
            }
        }

        IList<string> IFacet.Values
        {
            get
            {
                return this.Values;
            }
        }
    }
}
