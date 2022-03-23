using Sdl.Web.Common.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SDL.ECommerce.DXA.Example.Views.Models
{
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "FallbackArticle", Prefix = "e")]
    public class FallbackArticle : EntityModel 
    {
        [SemanticProperty("e:title")]
        public string Title { get; set; }

        [SemanticProperty("e:introduction")]
        public RichText Introduction { get; set; }

        [SemanticProperty("e:content")]
        public RichText Content { get; set; }
    }
}