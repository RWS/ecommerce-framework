using Sdl.Web.Common.Logging;
using Sdl.Web.Common.Models;
using System;
using System.Web.Mvc;
using SDL.ECommerce.Api;
using SDL.ECommerce.DXA.Factories;
using SDL.ECommerce.DXA.Servants;
using System.Collections.Generic;
using System.Linq;
using Sdl.Web.Mvc.Configuration;

namespace SDL.ECommerce.DXA.Controllers
{
    /// <summary>
    /// E-Commerce Product Page Controller
    /// </summary>
    public class ProductPageController : AbstractECommercePageController
    {
        private readonly IECommerceClient _eCommerceClient;

        private readonly IPathServant _pathServant;

        public ProductPageController()
        {
            _eCommerceClient = DependencyFactory.Current.Resolve<IECommerceClient>();
            _pathServant = DependencyFactory.Current.Resolve<IPathServant>();
        }

        /// <summary>
        /// Product Page controller action.
        /// Finds the product and resolve correct template page for displaying the product.
        /// </summary>
        /// <param name="productUrl"></param>
        /// <returns></returns>
        public virtual ActionResult ProductPage(string productUrl)
        {
            if (string.IsNullOrEmpty(productUrl))
            {
                return NotFound();
            }

            var urlSegments = ParseUrl(productUrl);
            
            if (urlSegments.IsInvalid)
            {
                Log.Warn("Invalid product URL: " + productUrl);

                return NotFound();
            }

            IProduct product = null;
            
            if (urlSegments.VariantAttributes.Any())
            {
                product = _eCommerceClient.DetailService.GetDetail(urlSegments.ProductId, urlSegments.VariantAttributes);
            }

            if (product == null)
            {
                return CreateVariantMissingActionResult(productUrl, urlSegments);
            }

            return CreateProductActionResult(productUrl, urlSegments, product);
        }

        protected virtual ActionResult CreateVariantMissingActionResult(string productUrl, UrlSegments urlSegments)
        {
            // TODO: Should we redirect to an URL without variants here? So it's clear for the user what's happening??
            var product = _eCommerceClient.DetailService.GetDetail(urlSegments.ProductId);

            if (product == null)
            {
                Log.Info("Product not found: " + urlSegments.ProductId);

                return NotFound();
            }

            return CreateProductActionResult(productUrl, urlSegments, product);
        }

        protected virtual ActionResult CreateProductActionResult(string productUrl, UrlSegments urlSegments, IProduct product)
        {
            PageModel templatePage = PageModelServant.ResolveTemplatePage(_pathServant.GetSearchPath(urlSegments.SeoId, product), ContentProvider);

            if (templatePage == null)
            {
                Log.Error("Product template page could not be found for product URL: " + productUrl);

                return NotFound();
            }

            templatePage.Title = product.Name;
            ECommerceContext.Set(ECommerceContext.PRODUCT, product);
            ECommerceContext.Set(ECommerceContext.URL_PREFIX, ECommerceContext.LocalizePath("/c"));
            WebRequestContext.PageModel = templatePage;
            return View(templatePage);
        }

        protected virtual UrlSegments ParseUrl(string productUrl)
        {
            var pathTokens = productUrl.Split(new[] {'/'}, StringSplitOptions.RemoveEmptyEntries);

            string productSeoId;

            string productId;

            if (pathTokens.Length == 2)
            {
                productSeoId = pathTokens[0];

                productId = pathTokens[1];
            }
            else if (pathTokens.Length == 1)
            {
                productSeoId = null;

                productId = pathTokens[0];
            }
            else
            {
                return new UrlSegments(null);
            }

            var queryParams = HttpContext.Request.QueryString;

            if (queryParams.Count > 0)
            {
                var variantAttributes = queryParams.AllKeys.ToDictionary(k => k, k => queryParams[k]);

                return new UrlSegments(productId, productSeoId, variantAttributes);
            }

            return new UrlSegments(productId, productSeoId);
        }

        protected class UrlSegments
        {
            public UrlSegments(string productId, string seoId = null, IDictionary<string, string> variantAttributes = null)
            {
                ProductId = productId;
                SeoId = seoId;
                VariantAttributes = variantAttributes ?? new Dictionary<string, string>();
            }

            public string ProductId { get; }
            
            public string SeoId { get; }
            
            public IDictionary<string, string> VariantAttributes { get; }

            public bool IsInvalid => string.IsNullOrEmpty(ProductId) && string.IsNullOrEmpty(SeoId) && !VariantAttributes.Any();
        }
    }
}