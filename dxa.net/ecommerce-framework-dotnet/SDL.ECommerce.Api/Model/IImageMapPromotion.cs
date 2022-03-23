using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
    public interface IImageMapPromotion : IContentPromotion
    {
        IList<IContentArea> ContentAreas { get; }
    }
}
