using System.Collections.Generic;

using Sdl.Web.Common.Logging;
using System.Web.Mvc;
using System.Web.Routing;

namespace SDL.ECommerce.DXA
{
    public class ECommerceControllerRegistration
    {
       
        const string NAMESPACE = "SDL.ECommerce.DXA.Controllers";

        public static void RegisterControllers(RouteCollection routes)
        {
            Log.Info("Registering E-Commerce Controllers...");

            // E-Commerce Page Controllers
            //
            MapRoute(routes, "ECommerce_Category", "c/{*categoryUrl}", 
                new { controller = "CategoryPage", action = "CategoryPage" });

            MapRoute(routes, "ECommerce_Page", "p/{*productUrl}",
                new { controller = "ProductPage", action = "ProductPage" });

            MapRoute(routes, "ECommerce_SearchPage", "search/{searchPhrase}/{*categoryUrl}",
                new { controller = "SearchPage", action = "SearchCategoryPage" });

            MapRoute(routes, "ECommerce_Search", "search/_redirect",
               new { controller = "SearchPage", action = "Search" });

            // Cart controller
            //
            MapRoute(routes, "ECommerce_AddToCart", "ajax/cart/addProduct/{productId}",
                new { controller = "Cart", action = "AddProductToCart" });

            MapRoute(routes, "ECommerce_RemoveFromCart", "ajax/cart/removeProduct/{productId}",
                new { controller = "Cart", action = "RemoveProductFromCart" });


            // Localization routes
            //
            MapRoute(routes, "ECommerce_Category_Loc", "{localization}/c/{*categoryUrl}",
                new { controller = "CategoryPage", action = "CategoryPage" });

            MapRoute(routes, "ECommerce_Page_Loc", "{localization}/p/{*productUrl}",
                new { controller = "ProductPage", action = "ProductPage" });

            MapRoute(routes, "ECommerce_SearchPage_Loc", "{localization}/search/{searchPhrase}/{*categoryUrl}",
                new { controller = "SearchPage", action = "SearchCategoryPage" });

            MapRoute(routes, "ECommerce_Search_Loc", "{localization}/search/_redirect",
               new { controller = "SearchPage", action = "Search" });

            MapRoute(routes, "ECommerce_AddToCart_Loc", "{localization}/ajax/cart/addProduct/{productId}",
                new { controller = "Cart", action = "AddProductToCart" });

            MapRoute(routes, "ECommerce_RemoveFromCart_Loc", "{localization}/ajax/cart/removeProduct/{productId}",
                new { controller = "Cart", action = "RemoveProductFromCart" });

            // Resolve routes
            //
            MapRoute(routes, "ECommerce_Resolve", "ecom-resolve",
               new { controller = "Resolve", action = "Resolve" });

            MapRoute(routes, "ECommerce_Resolve_Loc", "{localization}/ecom-resolve",
               new { controller = "Resolve", action = "Resolve" });


            // Edit Proxy route (only available for staging sites)
            //
            MapRoute(routes, "ECommerce_EditProxy", "edit-proxy/{*path}", new { controller = "EditProxy", action = "Http" });   

        }

        /// <summary>
        /// Map route for a page controller. 
        /// As this is called after the global DXA initialization we have to shuffle around the route definition so it comes before the DXA page controller.
        /// </summary>
        /// <param name="context"></param>
        /// <param name="name"></param>
        /// <param name="url"></param>
        /// <param name="defaults"></param>
        protected static void MapRoute(RouteCollection routes, string name, string url, object defaults)
        {
            Route route = new Route(url, new MvcRouteHandler())
            {
                Defaults = CreateRouteValueDictionary(defaults),
                DataTokens = new RouteValueDictionary()
                {
                    { "Namespaces", NAMESPACE}
                }
            };
            routes.Insert(0, route);
        }

        private static RouteValueDictionary CreateRouteValueDictionary(object values)
        {
            var dictionary = values as IDictionary<string, object>;
            if (dictionary != null)
            {
                return new RouteValueDictionary(dictionary);
            }

            return new RouteValueDictionary(values);
        }

    }
}
