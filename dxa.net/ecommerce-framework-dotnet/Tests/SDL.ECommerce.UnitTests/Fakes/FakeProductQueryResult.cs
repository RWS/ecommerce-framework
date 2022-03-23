namespace SDL.ECommerce.UnitTests.Fakes
{
    using System.Collections.Generic;
    using ECommerce.Api.Model;

    public class FakeProductQueryResult : IProductQueryResult
    {
        public int TotalCount { get; }

        public int StartIndex { get; }

        public int ViewSize { get; }

        public int CurrentSet { get; }

        public ILocation RedirectLocation { get; }

        public ICollection<IQuerySuggestion> QuerySuggestions { get; }

        public ICollection<IFacetGroup> FacetGroups { get; }

        public ICollection<IProduct> Products { get; }

        public ICollection<IPromotion> Promotions { get; }

        public ICollection<IBreadcrumb> Breadcrumbs { get; }
    }
}