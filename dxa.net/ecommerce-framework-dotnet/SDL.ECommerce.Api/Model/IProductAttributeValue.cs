using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Api.Model
{
    /// <summary>
    /// Product Attribute Value
    /// </summary>
    public interface IProductAttributeValue
    {
        /// <summary>
        /// Raw value of the product value
        /// </summary>
        string Value { get; }

        /// <summary>
        /// Presentation friendly value. Can be localized.
        /// </summary>
        string PresentationValue { get; }
    }
}
