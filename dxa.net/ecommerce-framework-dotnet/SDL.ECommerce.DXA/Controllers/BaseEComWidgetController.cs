using Sdl.Web.Common.Models;
using Sdl.Web.Mvc.Controllers;
using Sdl.Web.Common.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Mvc;
using SDL.ECommerce.DXA.Factories;
using SDL.ECommerce.DXA.Models;
using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Model;
using Sdl.Web.Mvc.Configuration;
using System.Runtime.Caching;
using System.Web.Configuration;

namespace SDL.ECommerce.DXA.Controllers
{
    /// <summary>
    /// Base Controller for E-Commerce widgets such as listers, facets, breadcrumbs etc
    /// </summary>
    public abstract class BaseEComWidgetController : BaseController
    {
        // Temporary solution until OData has full support for DXA caching
        //
        private string _clientType =  WebConfigurationManager.AppSettings["ecommerce-service-client-type"] ?? "odata";

        private IECommerceClient _eCommerceClient;

        protected BaseEComWidgetController()
        {
            LinkResolver = DependencyFactory.Current.Resolve<IECommerceLinkResolver>();
            _eCommerceClient = DependencyFactory.Current.Resolve<IECommerceClient>();
        }

        protected IECommerceLinkResolver LinkResolver { get; }

        /// <summary>
        /// Product Detail
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult ProductDetail(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);

            ProductDetailWidget widget = (ProductDetailWidget) entity;

            if (widget.ProductReference != null)
            {
                string productId;
                if ( widget.ProductReference.ProductRef != null )
                {
                    productId = widget.ProductReference.ProductRef.ExternalId;
                }
                else
                {
                    productId = widget.ProductReference.ProductId;
                }

                // Get product details from E-Commerce service
                //
                widget.Product = ECommerceContext.Client.DetailService.GetDetail(productId);
                
                // TODO: Add error handling when product is not found
            }
            else
            {
                // Use product from page controller
                //
                widget.Product = (IProduct) ECommerceContext.Get(ECommerceContext.PRODUCT);
            }

            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// ECL Product Detail
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult ProductDetailEclItem(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);

            ECommerceEclItem eclItem = (ECommerceEclItem)entity;
            eclItem.Product = ECommerceContext.Client.DetailService.GetDetail(eclItem.ExternalId);

            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Product Lister
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult ProductLister(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);

            ProductListerWidget widget = (ProductListerWidget) entity;
            IProductQueryResult queryResult = null;

            if (widget.CategoryReferences?.Any() ?? false)
            {
                var query = new Api.Model.Query
                {
                    ViewSize = widget.ViewSize,
                    CategoryIds = new List<string>()
                };

                if (widget.CategoryReferences.Count == 1)
                {
                    query.CategoryId = ResolveCategoryId(widget.CategoryReferences.First());
                }
                else
                {
                    foreach (var eCommerceCategoryReference in widget.CategoryReferences)
                    {
                        query.CategoryIds.Add(ResolveCategoryId(eCommerceCategoryReference));
                    }
                }
                if (widget.ContextData != null)
                {
                    query.ContextData = widget.ContextData.ToDictionary(c => c.Name, c => c.Value);
                }

                queryResult = _eCommerceClient.QueryService.Query(query);
            }
            else
            {
                // Use category from page controller
                //
                queryResult = (IProductQueryResult) ECommerceContext.Get(ECommerceContext.QUERY_RESULT);
            }
            if (queryResult == null)
            {
                queryResult = GetResultFromPageTemplate();
            }

            if (queryResult != null)
            {
                widget.Items = queryResult.Products?.ToList();
                this.ProcessListerNavigationLinks(widget, queryResult, (IList<FacetParameter>)ECommerceContext.Get(ECommerceContext.FACETS));
            }
            else
            {
                widget.Items = new List<IProduct>();
                widget.NavigationData = new ListerNavigationData
                {
                    ShowNavigation = false
                };
            }

            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Facets
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult Facets(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);
            FacetsWidget widget = (FacetsWidget) entity;

            // Get facets
            //
            IProductQueryResult queryResult = null;
            if ( widget.CategoryReference != null )
            {
                var category = ResolveCategory(widget.CategoryReference);
                queryResult = ECommerceContext.Client.QueryService.Query(new Api.Model.Query { Category = category });
            }
            else
            {
                // Use category from page controller
                //
                queryResult = (IProductQueryResult)ECommerceContext.Get(ECommerceContext.QUERY_RESULT);
            }
            if (queryResult == null)
            {
                queryResult = GetResultFromPageTemplate();
            }
            widget.FacetGroups = queryResult.FacetGroups.ToList();

            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Flyout Facets
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult FlyoutFacets(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);
            FacetsWidget widget = (FacetsWidget)entity;

            bool useDxaCaching = _clientType.Equals("rest");

            if ( widget.CategoryReference != null )
            {
                widget.CategoryReference.Category = ResolveCategory(widget.CategoryReference);
                if (widget.CategoryReference.Category != null)
                {
                    var cachedData = useDxaCaching ? null : this.GetCachedFlyoutData(widget.CategoryReference.Category.Id, WebRequestContext.Localization.Id);
                    if (cachedData == null)
                    {
                        var queryResult = ECommerceContext.Client.QueryService.Query(
                            new Api.Model.Query
                            {
                                Category = widget.CategoryReference.Category,
                                ViewType = Api.Model.ViewType.FLYOUT
                            });

                        // Tempory workaround
                        if (!useDxaCaching)
                        {
                            cachedData = new FlyoutData
                            {
                                FacetGroups = queryResult.FacetGroups.ToList(),
                                Promotions = queryResult.Promotions.ToList()
                            };
                            this.CacheFlyoutData(widget.CategoryReference.Category.Id, WebRequestContext.Localization.Id, cachedData);
                        }
                        else
                        {
                            widget.FacetGroups = queryResult.FacetGroups.ToList();
                            widget.RelatedPromotions = queryResult.Promotions.ToList();
                        }
                    }
                    // Temporary workaround
                    if (!useDxaCaching)
                    {
                        widget.FacetGroups = cachedData.FacetGroups;
                        widget.RelatedPromotions = cachedData.Promotions;
                    }
                }
            }
            
            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Promotions
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult Promotions(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);
            PromotionsWidget widget = (PromotionsWidget)entity;

            // Get promotions
            //
            IProductQueryResult queryResult = null;
            if (widget.CategoryReference != null)
            {
                var query = new Api.Model.Query();
                var category = ResolveCategory(widget.CategoryReference);
                query.Category = category;
                if ( widget.ViewType != null )
                {
                    query.ViewType = (Api.Model.ViewType) Enum.Parse(typeof(Api.Model.ViewType), widget.ViewType.ToUpper());
                }
                queryResult = ECommerceContext.Client.QueryService.Query(query);
            }
            else
            {
                // Use category from page controller
                //
                queryResult = (IProductQueryResult)ECommerceContext.Get(ECommerceContext.QUERY_RESULT);
            }
            if ( queryResult == null )
            {
                queryResult = GetResultFromPageTemplate();
            }

            if ( queryResult != null )
            {
                widget.Promotions = queryResult.Promotions.ToList();
            }
            else
            {
                var product = (IProduct)ECommerceContext.Get(ECommerceContext.PRODUCT);
                if ( product != null )
                {
                    widget.Promotions = product.Promotions;
                }
                else
                {
                    widget.Promotions = Enumerable.Empty<IPromotion>().ToList();
                }               
            }
            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Breadcrumb
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult Breadcrumb(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);
            BreadcrumbWidget widget = (BreadcrumbWidget)entity;

            // Get breadcrumb
            //
            IProductQueryResult queryResult = null;
            if (widget.CategoryReference != null)
            {
                var category = ResolveCategory(widget.CategoryReference);
                queryResult = ECommerceContext.Client.QueryService.Query(new Api.Model.Query { Category = category });
            }
            else
            {
                // Use category from page controller
                //
                queryResult = (IProductQueryResult)ECommerceContext.Get(ECommerceContext.QUERY_RESULT);
            }
            if (queryResult == null)
            {
                queryResult = GetResultFromPageTemplate();
            }

            if ( queryResult != null )
            {
                widget.Breadcrumbs = queryResult.Breadcrumbs.ToList();
                widget.TotalItems = queryResult.TotalCount;
            }
            else
            {
                var product = (IProduct)ECommerceContext.Get(ECommerceContext.PRODUCT);
                if ( product != null )
                {
                    widget.Breadcrumbs = product.Breadcrumbs;
                }
                else
                {
                    widget.Breadcrumbs = Enumerable.Empty<IBreadcrumb>().ToList();
                }            
            }
            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Search Feedback
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult SearchFeedback(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);
            SearchFeedbackWidget widget = (SearchFeedbackWidget)entity;

            var queryResult = (IProductQueryResult)ECommerceContext.Get(ECommerceContext.QUERY_RESULT);
            if ( queryResult != null && queryResult.QuerySuggestions != null )
            {
                widget.QuerySuggestions = queryResult.QuerySuggestions.ToList();
            }
            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Cart
        /// </summary>
        /// <param name="entity"></param>
        /// <param name="containerSize"></param>
        /// <returns></returns>
        public virtual ActionResult Cart(EntityModel entity, int containerSize = 0)
        {
            SetupViewData(entity, containerSize);
            CartWidget widget = (CartWidget)entity;
            widget.Cart = ECommerceContext.Cart;
            return View(entity.MvcData.ViewName, entity);
        }

        /// <summary>
        /// Resolve category via a CMS category reference
        /// </summary>
        /// <param name="categoryReference"></param>
        /// <returns></returns>
        protected ICategory ResolveCategory(ECommerceCategoryReference categoryReference)
        {
            ICategory category = null;
            if ( categoryReference.CategoryPath != null )
            {
                category = _eCommerceClient.CategoryService.GetCategoryByPath(categoryReference.CategoryPath);
            }
            else if ( categoryReference.CategoryId != null )
            {
                category = _eCommerceClient.CategoryService.GetCategoryById(categoryReference.CategoryId);
            }
            else if ( categoryReference.CategoryRef != null )
            {
                category = _eCommerceClient.CategoryService.GetCategoryById(categoryReference.CategoryRef.ExternalId);
            }
            return category;
        }

        /// <summary>
        /// Resolve category id via a CMS category reference
        /// </summary>
        /// <param name="categoryReference"></param>
        /// <returns></returns>
        protected string ResolveCategoryId(ECommerceCategoryReference categoryReference)
        {
            if (categoryReference?.CategoryId != null)
            {
                return categoryReference.CategoryId;
            }

            if (categoryReference?.CategoryRef != null)
            {
                return categoryReference.CategoryRef.ExternalId;
            }

            if (categoryReference?.CategoryPath != null)
            {
                return _eCommerceClient.CategoryService.GetCategoryByPath(categoryReference.CategoryPath)?.Id;
            }

            return null;
        }

        protected IProductQueryResult GetResultFromPageTemplate()
        {
            String requestPath = HttpContext.Request.Path;
            if (requestPath.StartsWith(ECommerceContext.LocalizePath("/categories")))
            {
                var category = this.GetCategoryFromPageTemplate(requestPath);
                var query = new Api.Model.Query
                {
                    Category = category,
                    Facets = ECommerceContext.Get(ECommerceContext.FACETS) as IList<FacetParameter>
                };
                return ECommerceContext.Client.QueryService.Query(query);
            }
            return null;
        }

        /// <summary>
        /// Extracts the category from the page template path.
        /// </summary>
        /// <param name="requestPath"></param>
        /// <returns></returns>
        protected ICategory GetCategoryFromPageTemplate(String requestPath)
        {
            Log.Debug("Trying to extract category from page template: " + requestPath);
            // Try to get query result based on the page url cat1-cat2-cat3
            //
            var categoryPath = "/" + requestPath.Replace(ECommerceContext.LocalizePath("/categories/"), "").Replace(".html", "").Replace("-", "/");
            var category = ECommerceContext.Client.CategoryService.GetCategoryByPath(categoryPath);
            if (category == null)
            {
                // Try with category ID
                //
                category = ECommerceContext.Client.CategoryService.GetCategoryById(categoryPath.Replace("/", ""));
            }
            return category;
        }

        /// <summary>
        /// Process navigation links in product listers (next, previous etc).
        /// </summary>
        /// <param name="lister"></param>
        /// <param name="result"></param>
        /// <param name="facets"></param>
        protected void ProcessListerNavigationLinks(ProductListerWidget lister, IProductQueryResult result, IList<FacetParameter> facets)
        {

            int totalCount = result.TotalCount;
            int viewSize = result.ViewSize;
            int startIndex = result.StartIndex;
            int currentSet = result.CurrentSet;

            lister.NavigationData = new ListerNavigationData 
            {
                TotalCount = totalCount
            };

            int viewSets = 1;
            if (totalCount > viewSize)
            {
                viewSets = (totalCount / viewSize) + 1;
            }
            lister.NavigationData.ViewSets = viewSets;

            if (viewSets > 1)
            {
                int nextStartIndex = -1;
                int previousStartIndex = -1;
                if (currentSet > 1)
                {
                    previousStartIndex = startIndex - viewSize;
                }
                if (currentSet < viewSets)
                {
                    nextStartIndex = startIndex + viewSize;
                }
                string baseUrl = LinkResolver.GetFacetLink(facets);
                if ( String.IsNullOrEmpty(baseUrl) )
                {
                    baseUrl += "?";
                }
                else
                {
                    baseUrl += "&";
                }

                if (previousStartIndex != -1)
                {
                    lister.NavigationData.PreviousUrl = baseUrl + "startIndex=" + previousStartIndex;
                }
                if (nextStartIndex != -1)
                {
                    lister.NavigationData.NextUrl = baseUrl + "startIndex=" + nextStartIndex;
                }
                if (currentSet > 2)
                {
                    lister.NavigationData.FirstUrl = baseUrl + "startIndex=0";
                }
                if (currentSet + 1 < viewSets)
                {
                    lister.NavigationData.LastUrl = baseUrl + "startIndex=" + ((viewSets - 1) * viewSize);
                }
                lister.NavigationData.CurrentSet = currentSet;
                lister.NavigationData.ShowNavigation = true;
            }
            else
            {
                lister.NavigationData.ShowNavigation = false;
            }

        }

        private FlyoutData GetCachedFlyoutData(string categoryId, string LocalizationId)
        {
            return this.flyoutCache[string.Concat(categoryId, "-", LocalizationId)] as FlyoutData;
        }

        private void CacheFlyoutData(string categoryId, string LocalizationId, FlyoutData flyoutData)
        {
            // Default cache flyout data in 1 hour. TODO: Have this configurable
            //
            this.flyoutCache.Add(string.Concat(categoryId, "-", LocalizationId), flyoutData, DateTimeOffset.Now.AddHours(1.0));
        }

        private ObjectCache flyoutCache = MemoryCache.Default;
    }

    internal class FlyoutData
    {
        public IList<IFacetGroup> FacetGroups { get; set; }
        public IList<IPromotion> Promotions { get; set; }
    }
}
