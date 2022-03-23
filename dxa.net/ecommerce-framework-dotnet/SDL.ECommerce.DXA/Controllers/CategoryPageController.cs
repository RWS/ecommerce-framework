namespace SDL.ECommerce.DXA.Controllers
{
    using Sdl.Web.Common.Logging;

    using System.Web.Mvc;
    using Api.Model;
    using Api.Service;
    using Sdl.Web.Common.Models;

    using SDL.ECommerce.Api;
    using SDL.ECommerce.DXA.Servants;
    using SDL.ECommerce.DXA.Factories;

    using Sdl.Web.Mvc.Configuration;
    using Query = Api.Model.Query;


    /// <summary>
    /// E-Commerce Category Page Controller
    /// </summary>
    public class CategoryPageController : AbstractECommercePageController
    {
        private readonly IECommerceClient _eCommerceClient;

        private readonly IECommerceLinkResolver _linkResolver;

        private readonly IHttpContextServant _httpContextServant;

        private readonly IPathServant _pathServant;

        public CategoryPageController()
        {
            _eCommerceClient = DependencyFactory.Current.Resolve<IECommerceClient>();
            _linkResolver = DependencyFactory.Current.Resolve<IECommerceLinkResolver>();
            _httpContextServant = DependencyFactory.Current.Resolve<IHttpContextServant>();
            _pathServant = DependencyFactory.Current.Resolve<IPathServant>();
        }

        public virtual ActionResult CategoryPage(string categoryUrl)
        {
            Log.Info("Entering category page controller with URL: " + categoryUrl);

            if (string.IsNullOrEmpty(categoryUrl))
            {
                categoryUrl = "/";
            }
            
            PageModel templatePage;

            var category = _eCommerceClient.CategoryService.GetCategoryByPath(categoryUrl);

            if (category != null)
            {
                templatePage = PageModelServant.ResolveTemplatePage(_pathServant.GetSearchPath(categoryUrl, category), ContentProvider);

                PageModelServant.SetTemplatePage(templatePage);

                templatePage = EnrichPageModelWithCategoryData(templatePage, category);

                SetupViewData(templatePage);
                WebRequestContext.PageModel = templatePage;

                var facets = _httpContextServant.GetFacetParametersFromRequest(HttpContext);

                var query = new Query
                                {
                                    Category = category,
                                    Facets = facets,
                                    StartIndex = _httpContextServant.GetStartIndex(HttpContext)
                                };

                PageModelServant.GetQueryContributions(templatePage, query);

                var searchResult = ExecuteQuery(_eCommerceClient.QueryService, query);

                if (searchResult?.RedirectLocation != null)
                {
                   return Redirect(_linkResolver.GetLocationLink(searchResult.RedirectLocation));
                }

                ECommerceContext.Set(ECommerceContext.CURRENT_QUERY, query);
                ECommerceContext.Set(ECommerceContext.QUERY_RESULT, searchResult);
                ECommerceContext.Set(ECommerceContext.URL_PREFIX, ECommerceContext.LocalizePath("/c"));
                ECommerceContext.Set(ECommerceContext.FACETS, facets);
                ECommerceContext.Set(ECommerceContext.CATEGORY, category);
            }
            else
            {
                Log.Warn("Category page with URL: /" + categoryUrl + " does not exists.");

                return NotFound();
            }

            return View(templatePage);
        }

        protected virtual PageModel EnrichPageModelWithCategoryData(PageModel templatePage, ICategory category)
        {
            templatePage.Title = category.Name;

            return templatePage;
        }

        protected virtual IProductQueryResult ExecuteQuery(IProductQueryService productQueryService, Query query)
        {
            return productQueryService.Query(query);
        }
    }
}