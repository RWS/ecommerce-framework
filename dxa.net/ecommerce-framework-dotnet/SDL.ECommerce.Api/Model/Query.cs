using System.Collections.Generic;

namespace SDL.ECommerce.Api.Model
{
  
    public class Query
    {  
        public Query()
        {
            Facets = new List<FacetParameter>();
        } 
        
        public Query(Query query)
        {
            Category = query.Category;
            CategoryId = query.CategoryId;
            Categories = query.Categories;
            CategoryIds = query.CategoryIds;
            SearchPhrase = query.SearchPhrase;
            Facets = query.Facets;
            ViewSize = query.ViewSize;
            StartIndex = query.StartIndex;
            ContextData = query.ContextData;
        }
             
        public ICategory Category { get; set; }
        public string CategoryId { get; set; }
        public IList<ICategory> Categories { get; set; }
        public IList<string> CategoryIds { get; set; }
        public string SearchPhrase { get; set; }
        public IList<FacetParameter> Facets { get; set; }
        public int? ViewSize { get; set; }   
        public int? StartIndex { get; set; }
        public ViewType? ViewType { get; set; }
        public IDictionary<string,string> ContextData { get; set; }

        // TODO: Add view type + filter attributes here

        public Query Next(IProductQueryResult queryResult)
        {
            var query = new Query(this);
            query.StartIndex = queryResult.StartIndex + queryResult.ViewSize;
            if ( query.StartIndex > queryResult.TotalCount )
            {
                return null;
            }
            return query;
        }

        public Query Previous(IProductQueryResult queryResult)
        {
            var query = new Query(this);
            query.StartIndex = queryResult.StartIndex - queryResult.ViewSize;
            if ( query.StartIndex < 0 )
            {
                return null;
            }
            return query;
        }

    }
}
