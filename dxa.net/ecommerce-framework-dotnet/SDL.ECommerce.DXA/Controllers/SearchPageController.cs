using Sdl.Web.Common.Logging;
using Sdl.Web.Common.Models;
using System.Collections.Generic;
using System.Web.Mvc;
using SDL.ECommerce.Api;
using SDL.ECommerce.DXA.Factories;

namespace SDL.ECommerce.DXA.Controllers
{
    /// <summary>
    /// E-Commerce Search Page Controller
    /// </summary>
    public class SearchPageController : AbstractECommercePageController
    {
        private readonly IECommerceLinkResolver _linkResolver;

        public SearchPageController()
        {
            _linkResolver = DependencyFactory.Current.Resolve<IECommerceLinkResolver>();
        }

        /// <summary>
        /// Search triggered by the search box
        /// </summary>
        /// <returns></returns>
        public virtual ActionResult Search()
        {
            var searchPhrase = HttpContext.Request.QueryString["q"];
            return Redirect(ECommerceContext.LocalizePath("/search/") + searchPhrase);
        }

        /// <summary>
        /// Handle search category page where the search results are presented
        /// </summary>
        /// <param name="searchPhrase"></param>
        /// <param name="categoryUrl"></param>
        /// <returns></returns>
        public virtual ActionResult SearchCategoryPage(string searchPhrase, string categoryUrl)
        {
            Log.Info("Entering search page controller with search phrase: " + searchPhrase + ", category: "  + categoryUrl);

            // Get facets
            //
            var facets = GetFacetParametersFromRequest();

            PageModel templatePage = null;

            // Build query
            //
            Api.Model.Query query;
            if ( categoryUrl != null )
            {
                var category = ECommerceContext.Client.CategoryService.GetCategoryByPath(categoryUrl);
                if (category != null)
                {
                    query = new Api.Model.Query { SearchPhrase = searchPhrase, Category = category, Facets = facets, StartIndex = GetStartIndex() };
                    ECommerceContext.Set(ECommerceContext.CATEGORY, category);
                }
                else
                {
                    return NotFound();
                }
            }
            else
            {
                query = new Api.Model.Query { SearchPhrase = searchPhrase, Facets = facets, StartIndex = GetStartIndex() };
            } 
            
            templatePage = this.ResolveTemplatePage(this.GetSearchPath());
            // templatePage.Title = ?? TODO: What title to use for search results?
            SetupViewData(templatePage);

            this.GetQueryContributions(templatePage, query);
            var searchResult = ECommerceContext.Client.QueryService.Query(query);

            if (searchResult.RedirectLocation != null)
            {
                return Redirect(_linkResolver.GetLocationLink(searchResult.RedirectLocation));
            }

            ECommerceContext.Set(ECommerceContext.CURRENT_QUERY, query);
            ECommerceContext.Set(ECommerceContext.QUERY_RESULT, searchResult);
            ECommerceContext.Set(ECommerceContext.SEARCH_PHRASE, searchPhrase);
            ECommerceContext.Set(ECommerceContext.URL_PREFIX, ECommerceContext.LocalizePath("/search/") + searchPhrase);
            ECommerceContext.Set(ECommerceContext.FACETS, facets);
            ECommerceContext.Set(ECommerceContext.ROOT_TITLE, "Search Results"); // TODO: Have this configurable in locale properties so it can be translated

            return View(templatePage);
        }

        /// <summary>
        /// Get search path to find an appropriate CMS template page for current search result
        /// </summary>
        /// <returns></returns>
        protected IList<string> GetSearchPath()
        {
            // TODO: Add more possibilities to override the look&feel for search results based on category and search phrases etc

            var searchPath = new List<string>();
            searchPath.Add(ECommerceContext.LocalizePath("/search-results"));
            return searchPath;
        }
    }
}