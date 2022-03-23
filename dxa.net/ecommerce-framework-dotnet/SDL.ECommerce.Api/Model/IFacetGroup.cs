using System;
using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{

    /// <summary>
    /// Facet Group.
    /// Represents a set of facets(e.g.brand, color, size etc).
    /// </summary>
           
    public interface IFacetGroup : IEditable
    {

        /// <summary>
        /// Unique identifier for this facet group
        /// </summary>
        string Id { get; }

        /// <summary>
        /// Title of the facet group. Should be localized to current language.
        /// </summary>
        /// <returns></returns>
        string Title { get; }

        /// <summary>
        ///  Get all belonging facets
        /// </summary>
        /// <returns></returns>
        ICollection<IFacet> Facets { get; }

        /// <summary>
        ///  Indicate if current facet group represents a product category or not.
        /// </summary>
        /// <returns></returns>
        bool IsCategory { get; }

        /// <summary>
        /// Associated attributes to control presentation of facets etc.
        /// </summary>
        ICollection<NameValue> Attributes { get; }
    }
}
