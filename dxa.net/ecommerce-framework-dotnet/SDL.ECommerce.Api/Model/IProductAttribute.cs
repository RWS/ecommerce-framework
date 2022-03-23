using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    public interface IProductAttribute
    {

        /// <summary>
        /// Attribute ID
        /// </summary>
        string Id { get; }

        /// <summary>
        /// Attribute name (presented to the user and can be localized)
        /// </summary>
        string Name { get; }

        /// <summary>
        /// If attribute is single value or not
        /// </summary>
        bool IsSingleValue { get; }

        /// <summary>
        /// Attribute values
        /// </summary>
        IList<IProductAttributeValue> Values { get; }

        /// <summary>
        /// Attribute value (if a single value)
        /// </summary>
        IProductAttributeValue Value { get; }

    }
}
