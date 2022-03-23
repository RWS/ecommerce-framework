using Microsoft.VisualStudio.TestTools.UnitTesting;
using SDL.ECommerce.Ecl;
using System;
using System.Xml.Linq;
using System.IO;
using System.Net;
using System.Security;
using System.Collections.Generic;

namespace SDL.Fredhopper.Ecl
{
    /// <summary>
    /// The tests are using the standard Fredhopper demo shop.
    /// Sample test properties are given in 'eclprovider-sample.runsettings'.
    /// </summary>
    [TestClass]
    public class UnitTests
    {
   
        public TestContext TestContext { get; set; }

        private ProductCatalog CreateProductCatalog()
        {
            var endpointAddress = TestContext.Properties["EndpointAddress"];
            var username = TestContext.Properties["UserName"];
            var password = TestContext.Properties["Password"];

            var configXml = "<Configuration xmlns=\"http://sdl.com/ecl/ecommerce\">" +
                    "<EndpointAddress>" + endpointAddress + "</EndpointAddress>";
            if (username != null && password != null)
            {
                configXml += "<UserName>" + username + "</UserName>" +
                             "<Password>" + password + "</Password>";
            }
            configXml +=
                    "<MaxItems>100</MaxItems>" +
                    "<CategoryMaxDepth>3</CategoryMaxDepth>" +
                    "<PublicationConfigurations>" +
                    "  <PublicationConfiguration publicationIds = \"1,2,3\" fallback=\"true\">" +
                    "    <Locale>en-GB</Locale>" +
                    "    <Universe>catalog01</Universe>" +
                    "    <ModelMappings>name=name;description=description;price=price;thumbnailUrl=_thumburl;primaryImageUrl=_imageurl</ModelMappings>" +
                    "  </PublicationConfiguration>" +
                    "</PublicationConfigurations></Configuration>";

            return new FredhopperProductCatalog(XElement.Parse(configXml));
        }

        [TestMethod]    
        public void TestGetCategories()
        {           
            var productCatalog = CreateProductCatalog();
            var categories = productCatalog.GetAllCategories();
            Console.WriteLine("Categories:");
            PrintCategoryList(categories, 1, 2);          
        }

        [TestMethod]
        public void TestIterateCategoryTree()
        {
            var productCatalog = CreateProductCatalog();
            var rootCategory = productCatalog.GetCategory(null);
            var cachedProduct = rootCategory.GetCachedCategory("catalog01_18661_17638");
            Console.WriteLine("Cached product: " + cachedProduct?.CategoryId);
            var paginatedList = new CategoryFlattenPaginatedList(rootCategory, 20, 4);
            var categories = paginatedList.Next(0);
            Console.WriteLine("Category IDs in flatten list:");
            foreach (var category in categories)
            {
                Console.WriteLine(category.CategoryId);
            }
            cachedProduct = rootCategory.GetCachedCategory("catalog01_18661_17638");
            Console.WriteLine("Cached product after traversing the tree: " + cachedProduct?.CategoryId);
        }

        private void PrintCategoryList(Category category, int level, int maxLevel)
        {
            if ( category.Categories == null ) { return; }
            foreach ( var subcategory in category.Categories)
            {
                for ( int i=0; i < level; i++ )
                {
                    Console.Write("-");
                }
                Console.WriteLine(subcategory.Title + "(" + subcategory.CategoryId + ")");
                if (level < maxLevel)
                {
                    PrintCategoryList(subcategory, level + 1, maxLevel);
                }
            }
        }

        [TestMethod]
        public void TestGetProductsUnderCategory()
        {           
            var productCatalog = CreateProductCatalog();
            var result = productCatalog.GetCategoryAndProducts("catalog01_18661_17638", 0);
            Console.WriteLine("Number of result pages: " + result.NumberOfPages);
            Console.WriteLine("Number of subcategories: " + result.Categories.Count);
            Console.WriteLine("Category products:");
            foreach (var product in result.Products)
            {
                Console.WriteLine(" " + product.Name + " (" + product.Id + ")");               
                if (product.Thumbnail != null)
                {
                    Console.WriteLine("   Image Thumbnail: " + product.Thumbnail.Url + ", " + product.Thumbnail.Mime);
                }
            }
           
        }

        [TestMethod]
        public void TestGetRootCategory()
        {
            var productCatalog = CreateProductCatalog();
            var result = productCatalog.GetCategoryAndProducts("catalog01_18661", 0);
            Console.WriteLine("Total number of products: " + result.Total);
            Console.WriteLine("Number of result pages: " + result.NumberOfPages);
        }

        [TestMethod]
        public void TestGetCategory()
        {
            var productCatalog = CreateProductCatalog();
            var category = productCatalog.GetCategory("catalog01_18664");
            Console.WriteLine("Category Title: " + category.Title);
            Console.WriteLine("Category Parent ID: " + category.Parent.CategoryId);
        }

        [TestMethod]
        public void TestSearch()
        {
            var productCatalog = CreateProductCatalog();
            var result = productCatalog.Search("Black", "catalog01_18661_62776", 0);
            Console.WriteLine("Products found:");
            foreach (var product in result.Products)
            {
                Console.WriteLine(" " + product.Name + " (" + product.Id + ")");
            }
            Console.WriteLine("Number of result pages: " + result.NumberOfPages);
        }

        [TestMethod]
        public void TestGetProduct()
        {
            var productCatalog = CreateProductCatalog();
            FredhopperProduct product = (FredhopperProduct) productCatalog.GetProduct("008010231960");
            Console.WriteLine("Product Name: " + product.Name);
            Console.WriteLine("Product Thumbnail URL: " + product.Thumbnail?.Url);
            Console.WriteLine("Product Thumbnail MIME: " + product.Thumbnail?.Mime);
            Console.WriteLine("Description:" + SecurityElement.Escape(product.Description));         
            Console.WriteLine("Price: " + product.Price);
            foreach ( var attribute in product.AdditionalAttributes )
            {
                var key = attribute.Key.Substring(0, 1).ToUpper() + attribute.Key.Substring(1);

                if (attribute.Value is List<ProductAttributeValue>)
                {
                    Console.WriteLine(key + ":");
                    foreach (var attrValue in attribute.Value as List<ProductAttributeValue>)
                    {
                        Console.WriteLine("  " + attrValue.PresentationValue);
                    }
                }
                else
                {
                    Console.WriteLine(key + ": " + ((ProductAttributeValue) attribute.Value).PresentationValue);
                }
            }
            if (product.Categories != null)
            {
                Console.WriteLine("Categories:");
                foreach (var category in product.Categories)
                {
                    Console.WriteLine("  " + category.Title + " (" + category.CategoryId + ")");
                }
            }
        }

        [TestMethod]
        public void TestReadProductImage()
        {
         
            var productCatalog = CreateProductCatalog();
            Product product = productCatalog.GetProduct("061010828997");
            using (WebClient webClient = new WebClient())
            {
                using (Stream stream = new MemoryStream(webClient.DownloadData(product.Thumbnail.Url)))
                {
                    Console.WriteLine("Content Length:" + stream.Length + ", Type: " + product.Thumbnail.Mime);
                }
            }
         
        }

    }
}
