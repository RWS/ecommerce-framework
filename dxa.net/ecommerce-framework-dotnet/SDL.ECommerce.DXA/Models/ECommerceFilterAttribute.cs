using Sdl.Web.Common.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SDL.ECommerce.DXA.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "ECommerceFilterAttribute", Prefix = "e")]
    public class ECommerceFilterAttribute : EntityModel
    {
        [SemanticProperty("e:name")]
        public string Name { get; set; }

        [SemanticProperty("e:value")]
        public string Value { get; set; }

        [SemanticProperty("e:mode")]
        public string Mode { get; set; }
    }
}