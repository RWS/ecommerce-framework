using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.OData
{
    public partial class ProductVariantAttributeValueType : IProductVariantAttributeValueType
    {
        bool IProductVariantAttributeValueType.IsSelected
        {
            get
            {
                return (bool) this.IsSelected;
            }
        }

        bool IProductVariantAttributeValueType.IsAvailable
        {
            get
            {
                return (bool)this.IsApplicable;
            }
        }

    }
} 
