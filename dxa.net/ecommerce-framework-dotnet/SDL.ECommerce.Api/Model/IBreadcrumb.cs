namespace SDL.ECommerce.Api.Model
{
    public interface IBreadcrumb
    {
        string Title { get; }
        bool IsCategory { get; }
        ICategoryRef CategoryRef { get; } 
        IFacet Facet { get; }
    }
}
