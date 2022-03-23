using Sdl.Web.Common.Models;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "ProductDetailWidget", Prefix = "e")]
    public class ProductDetailWidget : EntityModel
    {
        [SemanticProperty("e:product")]
        public ECommerceProductReference ProductReference { get; set; }

        [SemanticProperty("e:title")]
        public string Title { get; set; }

        [SemanticProperty("e:description")]
        public RichText Description { get; set; }

        [SemanticProperty("e:image")]
        public MediaItem image { get; set; } 

        // Enriched by the controller
        [SemanticProperty(IgnoreMapping =true)]
        public IProduct Product { get; set;  }
    }
}
