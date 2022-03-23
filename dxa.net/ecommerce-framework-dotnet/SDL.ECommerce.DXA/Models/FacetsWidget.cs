using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;

using System.Collections.Generic;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "FacetsWidget", Prefix = "e")]
    public class FacetsWidget : EntityModel
    {
        [SemanticProperty("e:category")]
        public ECommerceCategoryReference CategoryReference { get; set; }

        [SemanticProperty("e:viewType")]
        public string ViewType { get; set; }

        // TODO: Add filter attributes here!!!

        [SemanticProperty(IgnoreMapping = true)]
        public IList<IFacetGroup> FacetGroups { get; set; }

        [SemanticProperty(IgnoreMapping =true)]
        public IList<IPromotion> RelatedPromotions { get; set; }



    }
}