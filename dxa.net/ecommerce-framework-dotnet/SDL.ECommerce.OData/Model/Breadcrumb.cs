using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.OData
{
    public partial class Breadcrumb : IBreadcrumb
    {
        ICategoryRef IBreadcrumb.CategoryRef
        {
            get
            {
                return this.CategoryRef;
            }
        }

        IFacet IBreadcrumb.Facet
        {
            get
            {
                return this.Facet;
            }
        }

        bool IBreadcrumb.IsCategory
        {
            get
            {
                return (bool) this.IsCategory;
            }
        }
    }
}
