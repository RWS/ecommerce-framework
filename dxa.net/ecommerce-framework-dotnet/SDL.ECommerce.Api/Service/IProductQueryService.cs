﻿using SDL.ECommerce.Api.Model;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SDL.ECommerce.Api.Service
{
    public interface IProductQueryService
    {
        IProductQueryResult Query(Query query);
    }
}