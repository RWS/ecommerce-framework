using Sdl.Web.Common.Models;

namespace SDL.ECommerce.DXA.Models
{

    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "NameValuePair", Prefix = "e")]
    public class NameValuePair : EntityModel
    {
        [SemanticProperty("e:name")]
        public string Name { get; set; }

        [SemanticProperty("e:value")]
        public string Value { get; set; }
    }
}