using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Rest.Model
{
    public class MenuItem : IMenuItem
    {
        public string Title { get; set; }
        public string Url { get; set; }
    }
}
