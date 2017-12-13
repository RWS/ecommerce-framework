﻿using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    class FacetGroup : IFacetGroup
    {
        public string Id { get; set; }
        public string Title { get; set; }
        public string EditUrl { get; set; }
        public bool IsCategory { get; set; }
        public List<Facet> Facets { get; set; }

        ICollection<IFacet> IFacetGroup.Facets
        {
            get
            {
                return Facets.Cast<IFacet>().ToList();
            }
        }
   
    }
}
