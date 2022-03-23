using System;
using System.Collections.Generic;
using System.Linq;

using Microsoft.VisualStudio.TestTools.UnitTesting;

using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Model;
using SDL.ECommerce.OData;

namespace SDL.ECommerce.IntegrationTests.OData
{
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
                var endpointAddress = TestContext.Properties["EndpointAddress"] as string;
                var locale = TestContext.Properties["Locale"] as string;
                return new ECommerceClient(endpointAddress, locale, false);
            }
        }

        [TestMethod]
        public void TestGetTopLevelCategories()
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
        public void TestGetSpecificCategory()
        {
            var categoryService = ECommerceClient.CategoryService;
            var category = categoryService.GetCategoryById("catalog01_18661_128622");
            var category2 = categoryService.GetCategoryById("catalog01_18661_128622");
            Assert.IsTrue(category == category2, "Category data is not cached correctly");
           
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
        public void TestGetCategoryByPath()
        {
            var category = ECommerceClient.CategoryService.GetCategoryByPath("/women/accessories/watches");
            Console.WriteLine("  Category info (" + category.Id + ")");
            Console.WriteLine("#############################");
            Console.WriteLine("Category Name: " + category.Name);
        }

        [TestMethod]
        public void TestSearchWithSearchPhrase()
        {
            var query = new Query { SearchPhrase = "red" };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void TestSearchWithSearchPhraseAndCategory()
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
        public void TestSearchCategory()
        {
            var query = new Query { CategoryId = "catalog01_18661" };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void TestSearchWithFacets()
        {
            var query = new Query {
                CategoryId = "catalog01_18661",
                Facets = { new FacetParameter("brand", "dkny" ) }
            };
            var result = ECommerceClient.QueryService.Query(query);
            PrintQueryResult(result);
        }

        [TestMethod]
        public void TestSearches()
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
        public void TestGetProductDetails()
        {
            // TODO: Update with product detail result instead

            var product = ECommerceClient.DetailService.GetDetail("008010111647");
            Console.WriteLine("  Product: " + product.Id);
            Console.WriteLine("#########################");
            Console.WriteLine("Variant ID: " + product.VariantId);
            Console.WriteLine("Name: " + product.Name);
            Console.WriteLine("Description: " + product.Description);
            Console.WriteLine("Price: " + product.Price);
            Console.WriteLine("Image URL: " + product.PrimaryImageUrl);
            Console.WriteLine("Attributes: ");
            foreach ( var attribute in product.Attributes )
            {
                if (attribute.IsSingleValue)
                {
                    Console.WriteLine(attribute.Id + ": " + attribute.Value.PresentationValue + "(" + attribute.Value.Value + ")");
                }
                else
                {
                    Console.WriteLine(attribute.Id + ":");
                    foreach (var attributeValue in attribute.Values)
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
            Console.WriteLine("Promotions: ");
            foreach ( var promotion  in product.Promotions )
            {
                Console.WriteLine("  " + product.Name);
            }
        }
        
        [TestMethod]
        public void TestCart()
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
        public void TestGetInContextMenu()
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
            }
            Console.WriteLine(" Breadcrumbs:");
            Console.WriteLine("--------------------------");
            foreach ( var breadcrumb in result.Breadcrumbs )
            {
                Console.WriteLine("  Breadcrumb: " + breadcrumb.Title + " (category: " + breadcrumb.IsCategory + ")");
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
