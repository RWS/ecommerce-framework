using System;
using System.Collections.Generic;
using System.Web;

namespace SDL.ECommerce.Ecl
{
    public interface Category
    {
        string CategoryId { get; }
        string Title { get; }
        Category Parent { get; }
        IList<Category> Categories { get; }

        Category GetCachedCategory(string categoryId);
    }

    public class DummyCategory : Category
    {
        public DummyCategory(string categoryId)
        {
            CategoryId = categoryId;
        }

        public IList<Category> Categories
        {
            get
            {
                return new List<Category>();
            }
        }

        public string CategoryId
        {
            get; internal set;
        }

        public Category Parent
        {
            get
            {
                return null;
            }
        }

        public string Title
        {
            get
            {
                return "Category Not Found";
            }
        }

        public Category GetCachedCategory(string categoryId)
        {
            return null;
        }
    }

    public interface Product
    {
        string Id { get; }
        string Name { get; }
        ProductImage Thumbnail { get; }
        IList<Category> Categories { get; }
    }

    public class DummyProduct : Product
    {
        public DummyProduct(string id)
        {
            Id = id;
        }
        public IList<Category> Categories
        {
            get
            {
                return new List<Category>();
            }
        }

        public string Id
        {
            get; internal set;
        }

        public string Name
        {
            get
            {
                return "Product Not Found";
            }
        }

        public ProductImage Thumbnail
        {
            get
            {
                return null;
            }
        }
    }

    public interface ProductImage
    {
        string Url { get; }
        string Mime { get; }
    }

    public class StandardProductImage : ProductImage
    {
        private string url;
        private string mime;

        public StandardProductImage(string url)
        {
            this.url = url;
            this.mime = MimeMapping.GetMimeMapping(url);
        }

        public StandardProductImage(string url, string mime)
        {
            this.url = url;
            this.mime = mime;
        }

        public string Url { get { return this.url; } }
        public string Mime { get { return this.mime; } }
    }

    public class QueryResult
    {
        public int Total { get; set; }
        public int NumberOfPages { get; set; }
        public IList<Category> Categories { get; set; }
        public IList<Product> Products { get; set; }
    }

}
