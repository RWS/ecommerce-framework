using SDL.ECommerce.Api.Model;
using SDL.ECommerce.Api.Service;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class LazyCategory : ICategory
    {
        private IProductCategoryService _categoryService;
        private ICategory _category;

        public LazyCategory(string id, IProductCategoryService categoryService)
        {
            Id = id;
            _categoryService = categoryService;
        }

        private ICategory GetCategory()
        {
            if ( _category == null )
            {
               _category = _categoryService.GetCategoryById(Id);
            }
            return _category;
        }

        public string Id { get; internal set; }

        public IList<ICategory> Categories
        {
            get
            {
                return GetCategory().Categories;
            }
        }

        public string Name
        {
            get
            {
                return GetCategory().Name;
            }
        }

        public ICategory Parent
        {
            get
            {
                return GetCategory().Parent;
            }
        }

        public string PathName
        {
            get
            {
                return GetCategory().PathName;
            }
        }

        public string SanitizedPathName
        {
            get
            {
                return GetCategory().SanitizedPathName;
            }
        }
    }
}
