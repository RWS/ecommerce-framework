using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;

namespace SDL.ECommerce.OData
{
    public partial class Category : ICategory
    {
        private ICategory parent = null;
        private IList<ICategory> categories = null;
        private long expiryTime;

        ICategory ICategory.Parent
        {
            get
            {
                return this.parent;
            }
        }

        IList<ICategory> ICategory.Categories
        {
            get
            {
                return categories;
            }
        }

        public string SanitizedPathName { get; set; }

        internal void SetParent(ICategory parent)
        {
            this.parent = parent;
        }

        internal void SetCategories(IList<ICategory> categories, long expiryTime)
        {
            this.categories = categories;
            this.expiryTime= expiryTime;
        }

        internal bool NeedRefresh()
        {
            return this.categories == null || this.expiryTime < (DateTime.UtcNow.Ticks/ TimeSpan.TicksPerMillisecond); 
        }

    }
}
