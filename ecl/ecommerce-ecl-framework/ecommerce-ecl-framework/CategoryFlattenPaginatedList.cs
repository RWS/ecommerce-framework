using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Ecl
{
    /// <summary>
    /// Category Flatten Paginated List.
    /// Provided a flatten paginated list of all available categories
     /// </summary>
    public class CategoryFlattenPaginatedList
    {
        private Category rootCategory;
        private int pageSize;
        private int maxLevel;

        public CategoryFlattenPaginatedList(Category rootCategory, int pageSize, int maxLevel)
        {
            this.rootCategory = rootCategory;
            this.pageSize = pageSize;
            this.maxLevel = maxLevel;
        }

        public IList<Category> Next(int pageIndex)
        {
            var cursor = new TreeCursor
            {
                StartIndex = pageIndex * pageSize,
            };
            IterateCategoryTree(rootCategory, cursor, 0);
            return cursor.Categories;
        }

        private bool IterateCategoryTree(Category parentCategory, TreeCursor cursor, int level)
        {
            foreach (var category in parentCategory.Categories)
            {
                if (cursor.CurrentIndex >= cursor.StartIndex)
                {
                    cursor.Categories.Add(category);
                    if (cursor.Categories.Count >= pageSize)
                    {
                        return true;
                    }
                }
                cursor.CurrentIndex++;
                if (level < maxLevel)
                {
                    var reachedPageSize = IterateCategoryTree(category, cursor, level + 1);
                    if (reachedPageSize)
                    {
                        return true;
                    }
                }
                
            }
            return false;
        }


    }

    internal class TreeCursor
    {
        internal int StartIndex { get; set; }
        internal int CurrentIndex { get; set; }
        internal IList<Category> Categories { get; } = new List<Category>();
    }
}
