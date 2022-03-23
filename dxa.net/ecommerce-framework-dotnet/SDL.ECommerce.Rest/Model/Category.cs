using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    class Category : ICategory
    {
        private long expiryTime;
        private ICategory parent = null;
        private IList<ICategory> categories = null;

       
        public string Id { get; set; }
        public string Name { get; set; }      
        public string PathName { get; set; }
        public string SanitizedPathName { get; set; }
        public List<string> ParentIds { get; set; }

        IList<ICategory> ICategory.Categories
        {
            get
            {
                return categories;
            }
        }

        ICategory ICategory.Parent
        {
            get
            {
                return this.parent;
            }
        }

        internal void SetParent(ICategory parent)
        {
            this.parent = parent;
        }

        internal void SetCategories(IList<ICategory> categories, long expiryTime)
        {
            this.categories = categories;
            this.expiryTime = expiryTime;
        }

        internal bool NeedRefresh()
        {
            return this.categories == null || this.expiryTime < (DateTime.UtcNow.Ticks / TimeSpan.TicksPerMillisecond);
        }
    }
}
