namespace SDL.ECommerce.Api.Model
{
    public interface IProductVariantAttributeValueType
    {
        string Id { get; }

        string Value { get; }

        bool IsSelected { get; }

        bool IsAvailable { get; }
    }
}
