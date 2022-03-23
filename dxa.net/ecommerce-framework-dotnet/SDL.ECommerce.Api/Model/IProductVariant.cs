using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    /// <summary>
    /// Product Variant
    /// </summary>
    public interface IProductVariant
    {
        /// <summary>
        /// Variant Product ID
        /// </summary>
        string Id { get; }

        /// <summary>
        /// Variant Price
        /// </summary>
        IProductPrice Price { get; }

        /// <summary>
        /// Variant Attributes
        /// </summary>
        IList<IProductAttribute> Attributes { get; }

        // TODO: Can this be typed somehow??

        /*

        Get all all variants of type 'color'

        product.getVariantAttributeType.filter(name="color");

        */
    }
}
