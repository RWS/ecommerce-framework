using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;

namespace SDL.ECommerce.OData
{
    public partial class CategorySummary : ICategory
    {
        public IList<ICategory> Categories
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public ICategory Parent
        {
            get
            {
                throw new NotImplementedException();
            }
        }

        public string SanitizedPathName { get; set; }
    }
}
