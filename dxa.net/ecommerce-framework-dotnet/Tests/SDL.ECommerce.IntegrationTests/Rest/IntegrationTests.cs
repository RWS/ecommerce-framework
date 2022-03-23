using System;
using System.Collections.Generic;
using System.Linq;

using Microsoft.VisualStudio.TestTools.UnitTesting;

using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Model;
using SDL.ECommerce.Rest;

namespace SDL.ECommerce.IntegrationTests.Rest
{

    class DummyCacheProvider : IECommerceCacheProvider
    {
        private Dictionary<string, object> cache = new Dictionary<string, object>();

        public void Store<T>(CacheRegion region, string key, T value)
        {
            cache.Add(region.ToString() + "#" + key, value);
        }

        public bool TryGet<T>(CacheRegion region, string key, out T value)
        {
            object objValue;
            bool found = cache.TryGetValue(region.ToString() + "#" + key, out objValue);
            value = (T)objValue;
            return found;
        }
    }

    /// <summary>
    /// OData Client integration tests. The tests is based on an OData micro service instance having
    /// the Fredhopper connector installed using the standard Fredhopper demo dataset.
    /// </summary>
    [TestClass]
    public class IntegrationTests
    {
        public TestContext TestContext { get; set; }

        protected ECommerceClient ECommerceClient
        {
            get
            {
                /* TODO: Have this configurable in the test context
                var endpointAddress = TestContext.Properties["EndpointAddress"] as string;
                var locale = TestContext.Properties["Locale"] as string;
                return new ECommerceClient(endpointAddress, locale);
                */
                return new ECommerceClient("http://preview:8097/ecommerce.svc", "en-US", new DummyCacheProvider(), 3600000, false);
            }
        }

        [TestMethod]
        public void RestGetTopLevelCategories()
        {
            var categories = ECommerceClient.CategoryService.GetTopLevelCategories();
            Console.WriteLine("  Top level categories:");
            Console.WriteLine("#########################");
            foreach ( var category in categories )
            {
                Console.WriteLine("Category: " + category.Name + " (" + category.Id + ")");
            }
        }

        [TestMethod]
        public void RestGetSpecificCategory()
        {
            var categoryService = ECommerceClient.CategoryService;
            var category = categoryService.GetCategoryById("catalog01_18661_128622");
            var category2 = categoryService.GetCategoryById("catalog01_18661_128622");
            var category3 = categoryService.GetCategoryById("catalog01_18661_128622");
            Assert.IsTrue(category3 == category2, "Category data is not cached correctly");
           
            Console.WriteLine("  Category info (" + category.Id + ")");
            Console.WriteLine("#############################");
            Console.WriteLine("Category Name: " + category.Name);
            Console.WriteLine("Subcategories: ");
          
            foreach ( var subCategory in category.Categories )
            {
                Console.WriteLine("  " + subCategory.Name + "(" + subCategory.Id + ")");
            }
            
        }

        [TestMethod]
        public void RestGetCategoryByPath()
        {
            var category = ECommerceClient.CategoryService.GetCategoryByPath("/women/accessories/watches");
            Console.WriteLine("  Category info (" + category.Id + ")");
            Console.WriteLine("#############################");
            Console.WriteLine("Category Name: " + category.Name);
        }

        [TestMethod]
        public void RestSearchWithSearchPhrase()
        {
            var query = new Query { SearchPhrase = "red" };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestSearchWithSearchPhraseAndCategory()
        {
            var queryService = ECommerceClient.QueryService;
            var query = new Query { CategoryId = "catalog01_18661", SearchPhrase = "red" };
            var result = queryService.Query(query);
            PrintQueryResult(result);
            var nextResult = queryService.Query(query.Next(result));
            Console.WriteLine("\n======================================================");
            Console.WriteLine("      NEXT " + nextResult.ViewSize + " PRODUCTS ");
            Console.WriteLine("======================================================\n");
            PrintQueryResult(nextResult);
        }


        [TestMethod]
        public void RestSearchCategory()
        {
            var query = new Query { CategoryId = "catalog01_18661" };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestSearchCategoryWithContextData()
        {
            var query = new Query
            {
                CategoryId = "catalog01_18662",
                ContextData = new Dictionary<string, string> { { "fh_usertype", "multichannel" } }
            };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestSearchInvalidCategory()
        {
            var query = new Query { CategoryId = "catalog01_18661_ERROR" };
            try
            {
                var result = ECommerceClient.QueryService.Query(query);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception: " + e.Message);
            }
        }

        [TestMethod]
        public void RestSearchCategories()
        {
            var query = new Query
            {
                Categories = new List<ICategory>
                {
                    ECommerceClient.CategoryService.GetCategoryByPath("/women"),
                    ECommerceClient.CategoryService.GetCategoryByPath("/men")
                }
            };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestSearchInvalidCategories()
        {
            var query = new Query
            {
                CategoryIds = new List<string>
                {
                    "/women_ERROR",
                    "/men"
                }
            };
            try
            {
                var result = ECommerceClient.QueryService.Query(query);
            }
            catch (Exception e)
            {
                Console.WriteLine("Exception: " + e.Message);
            }

        }

        [TestMethod]
        public void RestSearchWithFacets()
        {
            var query = new Query {
                CategoryId = "catalog01_18661",
                Facets = { new FacetParameter("brand", "dkny" ) },
                ViewSize = 20
                
            };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestSearchCategoriesWithFacets()
        {
            var query = new Query
            {
                CategoryIds = new List<string> { "catalog01_18661", "catalog01_18664" },
                Facets = { new FacetParameter("brand", "adidas") },
                ViewSize = 20

            };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestSearches()
        {
            var queryService = ECommerceClient.QueryService;
            Console.WriteLine("Search request #1:");
            var query = new Query
            {
                CategoryId = "catalog01_18661"
            };
            var result = queryService.Query(query);
            Console.WriteLine("  Breadcrumbs: " + result.Breadcrumbs.Count);
            Console.WriteLine("Search request #2:");
            var query2 = new Query
            {
                CategoryId = "catalog01_18661",
                Facets = { new FacetParameter("brand", "dkny") }
            };
            var result2 = queryService.Query(query2);
            Console.WriteLine("  Breadcrumbs: " + result2.Breadcrumbs.Count);
            Console.WriteLine("Search request #3:");
            var query3 = new Query
            {
                CategoryId = "catalog01_18661"
            };
            var result3 = queryService.Query(query);
            Console.WriteLine("  Breadcrumbs: " + result3.Breadcrumbs.Count);
        }

        [TestMethod]
        public void RestSearchSuggestion()
        {
            var queryService = ECommerceClient.QueryService;
            var query = new Query
            {
                SearchPhrase = "red adidias"
            };
            var result = queryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void RestGetProductDetails()
        {
            // TODO: Update with product detail result instead

            var product = ECommerceClient.DetailService.GetDetail("008010111647");
            Console.WriteLine("  Product: " + product.Id);
            Console.WriteLine("#########################");
            Console.WriteLine("Variant ID: " + product.VariantId);
            Console.WriteLine("Name: " + product.Name);
            Console.WriteLine("Description: " + product.Description);
            
            Console.WriteLine("Price: " + product.Price);
            //Console.WriteLine("Image URL: " + product.PrimaryImageUrl);
            
            
            Console.WriteLine("Attributes: ");
            foreach ( var attribute in product.Attributes )
            {
                if ( attribute.IsSingleValue )
                {
                    Console.WriteLine(attribute.Id + ": " + attribute.Value.PresentationValue + "(" + attribute.Value.Value + ")");
                }
                else
                {
                    Console.WriteLine(attribute.Id + ":");
                    foreach ( var attributeValue in attribute.Values)
                    {
                        Console.WriteLine("  - " + attributeValue.PresentationValue);
                    }
                }              
            }
            
            Console.WriteLine("Categories: ");
            foreach ( var category in product.Categories )
            {
                Console.WriteLine("  " + category.Name);
            }
            Console.WriteLine("Breadcrumbs: ");
            foreach ( var breadcrumb in product.Breadcrumbs )
            {
                Console.WriteLine("  " + breadcrumb.Title);
            }
           
            // TODO: Check promotions in Fredhopper for details
            Console.WriteLine("Promotions: ");
            foreach ( var promotion  in product.Promotions )
            {
                Console.WriteLine("  " + product.Name);
            }
           
        }

        /*
        [TestMethod]
        public void RestGetProductDetailsWithVariants()
        {
            // TODO: Update with product detail result instead

            var product = ECommerceClient.DetailService.GetDetail("7135571");
            Console.WriteLine("Variants: ");
            foreach (var variant in product.Variants)
            {
                Console.WriteLine("  " + variant.Id);
            }
            Console.WriteLine("Attribute Types: ");
            foreach (var type in product.VariantAttributeTypes)
            {
                Console.WriteLine("  " + type.Id);
                foreach ( var value in type.Values)
                {
                    Console.WriteLine("    " + value.Id);
                }
            }
        }
        */

        [TestMethod]
        public void RestCart()
        {
            Console.WriteLine("Creating cart...");        
            var cart = ECommerceClient.CartService.CreateCart();
            PrintCart(cart);
            Console.WriteLine("Adding product to cart...");
            cart = ECommerceClient.CartService.AddProductToCart(cart, "008010111647", 1);
            PrintCart(cart);
            Console.WriteLine("Removing products from cart...");
            cart = ECommerceClient.CartService.RemoveProductFromCart(cart, "008010111647");
            PrintCart(cart);

        }

        [TestMethod]
        public void RestGetInContextMenu()
        {
            var menu = ECommerceClient.EditService.GetInContextMenuItems(new Query { CategoryId = "catalog01_18661", SearchPhrase = "red" });
            Console.WriteLine("  In-Context Edit Menu");
            Console.WriteLine("#########################");
            Console.WriteLine("Title: " + menu.Title);
            foreach ( var menuItem in menu.MenuItems )
            {
                Console.WriteLine("  " + menuItem.Title + " => " + menuItem.Url);
            }
        }

        void PrintQueryResult(IProductQueryResult result)
        {
            Console.WriteLine("  Search Results:");
            Console.WriteLine("####################");
            Console.WriteLine("Total Count: " + result.TotalCount);
            Console.WriteLine("Start index: " + result.StartIndex);
            Console.WriteLine("Current set: " + result.CurrentSet);
            Console.WriteLine("First " + result.ViewSize + " products:");
            foreach (var product in result.Products)
            {
                Console.WriteLine("Product ID: " + product.Id + ", Name: " + product.Name);
            }
            
            Console.WriteLine(" Facets:");
            Console.WriteLine("--------------------------");
            foreach (var facetGroup in result.FacetGroups)
            {
                Console.WriteLine("  FacetGroup: " + facetGroup.Title);
                Console.WriteLine("  Edit URL: " + facetGroup.EditUrl);
                if (facetGroup.Attributes != null)
                {
                    Console.WriteLine("  Attributes:");
                    foreach  (var attribute in facetGroup.Attributes) {
                        Console.WriteLine("    " + attribute.Name + ": " + attribute.Value);
                    }
                }
                foreach (var facet in facetGroup.Facets)
                {
                    Console.WriteLine("   " + facet.Title + " (" + facet.Count + ")");
                }
            }
            
            Console.WriteLine(" Promotions:");
            Console.WriteLine("--------------------------");
            foreach ( var promotion in result.Promotions )
            {
                Console.WriteLine("Name: " + promotion.Name);
                if ( promotion is IProductsPromotion)
                {
                    var productsPromotion = promotion as IProductsPromotion;
                    Console.WriteLine("Products:");
                    foreach ( var product in productsPromotion.Products)
                    {
                        Console.WriteLine("  Product ID: " + product.Id + ", Name: " + product.Name);
                    }
                }
                else if (promotion is IContentPromotion)
                {
                    var contentPromo = promotion as IContentPromotion;
                    Console.WriteLine("Image URL: " + contentPromo.ImageUrl);
                }
            }
            
            Console.WriteLine(" Breadcrumbs:");
            Console.WriteLine("--------------------------");
            foreach ( var breadcrumb in result.Breadcrumbs )
            {
                Console.WriteLine("  Breadcrumb: " + breadcrumb.Title + " (category: " + breadcrumb.IsCategory + ")");
            }
            if ( result.QuerySuggestions != null)
            {
                Console.WriteLine(" Query suggestions:");
                Console.WriteLine("--------------------------");
                foreach (var suggestion in result.QuerySuggestions)
                {
                    Console.WriteLine("  Suggestion: " + suggestion.Suggestion + "(Original: " + suggestion.Original + ")");
                }
            }

        }

        void PrintCart(ICart cart)
        {
            Console.WriteLine("  Cart Id=" + cart.Id);
            Console.WriteLine("#########################");
            if (cart.Items.Count > 0)
            {
                Console.WriteLine("Items:");
                
                foreach (var cartItem in cart.Items)
                {
                    Console.WriteLine("  " + cartItem.Product.Name + " " + cartItem.Price.FormattedPrice + " Quantity: " + cartItem.Quantity);
                }
                
                Console.WriteLine("Count: " + cart.Count);
                Console.WriteLine("Total Price: " + cart.TotalPrice.FormattedPrice);
            }
        }
    }
}
