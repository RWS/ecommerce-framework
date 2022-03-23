using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class Breadcrumb : IBreadcrumb
    {
        public string Title { get; set; }
        public CategoryRef CategoryRef { get; set; }
        public Facet Facet { get; set; }
        public bool Category { get; set; }

        ICategoryRef IBreadcrumb.CategoryRef
        {
            get
            {
                return CategoryRef;
            }
        }

        IFacet IBreadcrumb.Facet
        {
            get
            {
                return Facet;
            }
        }
       
        bool IBreadcrumb.IsCategory { get { return Category; } }
    }
}
