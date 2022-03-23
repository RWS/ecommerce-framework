using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class Facet : IFacet
    {
        public int Count { get; set; }

        public string Id { get; set; }

        public bool Category { get; set; }

        public bool IsCategory { get { return Category; } }

        public bool Selected { get; set; }

        public bool IsSelected { get { return Selected; } }

        public string Title { get; set; }

        public string Type { get; set; }

        FacetType IFacet.Type { get { return (FacetType)Enum.Parse(typeof(FacetType), this.Type); } }

        public string Value { get; set; }

        public IList<string> Values { get; set; }
    }
}
