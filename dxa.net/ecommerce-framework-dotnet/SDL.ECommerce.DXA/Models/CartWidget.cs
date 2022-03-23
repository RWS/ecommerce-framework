using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "CartWidget", Prefix = "e")]
    public class CartWidget : EntityModel
    {
        [SemanticProperty("e:_self")]
        public string CartPageLink { get; set; }

        [SemanticProperty("e:checkoutLink")]
        public Link CheckoutLink { get; set; }

        [SemanticProperty(IgnoreMapping = true)]
        public ICart Cart { get; set; }
    }
}