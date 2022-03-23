using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;

using System.Collections.Generic;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "PromotionsWidget", Prefix = "e")]
    public class PromotionsWidget : EntityModel
    {
        [SemanticProperty("e:category")]
        public ECommerceCategoryReference CategoryReference { get; set; }

        [SemanticProperty("e:product")]
        public ECommerceProductReference ProductReference { get; set; }

        [SemanticProperty("e:viewType")]
        public string ViewType { get; set; }

        [SemanticProperty("e:maxPromotions")]
        public int? MaxPromotions { get; set; }

        [SemanticProperty(IgnoreMapping = true)]
        public IList<IPromotion> Promotions { get; set; }
    }
}