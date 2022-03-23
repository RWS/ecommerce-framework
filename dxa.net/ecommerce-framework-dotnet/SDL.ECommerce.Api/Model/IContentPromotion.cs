namespace SDL.ECommerce.Api.Model
{
    /// <summary>
    /// Content Promotion
    /// </summary>
    public interface IContentPromotion : IPromotion
    {
        /// <summary>
        ///  Textual content of this promotion
        /// </summary>
        string Text { get; }

        /// <summary>
        ///  Image URL to the image to be used in this promotion
        /// </summary>
        string ImageUrl { get; }

        /// <summary>
        ///  Call-to-action location for this promotion.
        /// </summary>
        ILocation Location { get; }
    }
}
