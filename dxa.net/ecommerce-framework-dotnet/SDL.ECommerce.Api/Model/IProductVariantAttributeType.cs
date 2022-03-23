using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    public interface IProductVariantAttributeType
    {
        string Id { get; }

        string Name { get; }

        IList<IProductVariantAttributeValueType> Values { get; }

    }
}
