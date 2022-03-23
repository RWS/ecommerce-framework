using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    class QuerySuggestion : IQuerySuggestion
    {
        public string Original { get; set; }

        public string Suggestion { get; set; }

        public int? EstimatedResults { get; set; }
    }
}
