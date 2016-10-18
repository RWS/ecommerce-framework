﻿using Sdl.Web.Common.Models;
using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace SDL.ECommerce.DXA
{
    /// <summary>
    /// Search Feedback Widget
    /// </summary>
    [SemanticEntity(Vocab = CoreVocabulary, EntityName = "SearchFeedbackWidget", Prefix = "e")]
    public class SearchFeedbackWidget : EntityModel
    {
        [SemanticProperty("e:spellCheckLabel")]
        public RichText SpellCheckLabel { get; set; }

        [SemanticProperty(IgnoreMapping = true)]
        public IList<IQuerySuggestion> QuerySuggestions { get; set; } 
    }
}