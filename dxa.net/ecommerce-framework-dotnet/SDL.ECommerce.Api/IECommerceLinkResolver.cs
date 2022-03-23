using SDL.ECommerce.Api.Model;

using System.Collections.Generic;

namespace SDL.ECommerce.Api
{
    /// <summary>
    /// Interface for E-Commerce Link Resolver.
    /// </summary>
    public interface IECommerceLinkResolver
    {
        /// <summary>
        /// Get link to category page.
        /// </summary>
        /// <param name="category"></param>
        /// <returns></returns>
        string GetCategoryLink(ICategory category);

        /// <summary>
        /// Get a non-contextual link to category page, i.e. without based on info from page controllers etc.
        /// </summary>
        /// <param name="category"></param>
        /// <returns></returns>
        string GetNonContextualCategoryLink(ICategory category);

        /// <summary>
        /// Get facet link.
        /// </summary>
        /// <param name="facet"></param>
        /// <returns></returns>
        string GetFacetLink(IFacet facet);

        /// <summary>
        /// Get facet link based on a list of selected facet
        /// </summary>
        /// <param name="facetParameters"></param>
        /// <returns></returns>
        string GetFacetLink(IList<FacetParameter> selectedFacets);

        /// <summary>
        /// Get an absolute facet link using a base category path (for flyouts etc).
        /// </summary>
        /// <param name="facet"></param>
        /// <param name="navigationBasePath"></param>
        /// <returns></returns>
        string GetAbsoluteFacetLink(IFacet facet, string navigationBasePath);

        /// <summary>
        /// Get breadcrumb link.
        /// </summary>
        /// <param name="breadcrumb"></param>
        /// <returns></returns>
        string GetBreadcrumbLink(IBreadcrumb breadcrumb);

        /// <summary>
        /// Get link to location, normally a category + a set of facets).
        /// </summary>
        /// <param name="location"></param>
        /// <returns></returns>
        string GetLocationLink(ILocation location);

        /// <summary>
        /// Get link to product detail page.
        /// </summary>
        /// <param name="product"></param>
        /// <returns></returns>
        string GetProductDetailLink(IProduct product);

        /// <summary>
        /// Get link to a product variant page using products variant id. If the variant id is not available a fallback will be done on the product ID.
        /// </summary>
        /// <param name="product"></param>
        /// <returns></returns>
        string GetProductVariantDetailLink(IProduct product);

        /// <summary>
        /// Get link to product variant page using specified variant attribute
        /// </summary>
        /// <param name="product"></param>
        /// <param name="variantAttributeId"></param>
        /// <param name="variantAttributeValueId"></param>
        /// <param name="isPrimary"></param>
        /// <returns></returns>
        string GetProductDetailVariantLink(IProduct product, string variantAttributeId, string variantAttributeValueId, bool isPrimary);
    }
}
