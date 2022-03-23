using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class CategoryRef : ICategoryRef
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public string Path { get; set; }
    }
}
