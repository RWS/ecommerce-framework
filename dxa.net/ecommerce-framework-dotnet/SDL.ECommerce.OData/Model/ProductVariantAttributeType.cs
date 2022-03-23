using SDL.ECommerce.Api.Model;

using System.Collections.Generic;
using System.Linq;

namespace SDL.ECommerce.OData
{
    public partial class ProductVariantAttributeType : IProductVariantAttributeType
    {
        IList<IProductVariantAttributeValueType> IProductVariantAttributeType.Values
        {
            get
            {
                return this.Values.Cast<IProductVariantAttributeValueType>().ToList();
            }
        }
    }
}
