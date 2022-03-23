using SDL.ECommerce.Api.Service;
using System;
using System.Collections.Generic;
using SDL.ECommerce.Api.Model;
using RestSharp;
using Newtonsoft.Json;
using SDL.ECommerce.Rest.Model;
using Newtonsoft.Json.Serialization;

namespace SDL.ECommerce.Rest.Service
{
    public class CartService : ICartService
    {
        private IRestClient restClient;

        public CartService(IRestClient restClient)
        {
            this.restClient = restClient;
        }

        public ICart CreateCart()
        {
            var request = new RestRequest("/cart/", Method.POST);
            var response = this.restClient.Execute(request);
            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                return JsonConvert.DeserializeObject<Cart>(response.Content);
            }
            return null; // TODO: THROW EXCEPTION HERE!!! Have some generic REST code -> extensions???
        }

        public ICart AddProductToCart(ICart cart, string productId, int quantity)
        {
            var request = new RestRequest("/cart/" + cart.Id, Method.PUT);
            var input = JsonConvert.SerializeObject(
                new List<InputCartItem>()
                {
                    new InputCartItem
                    {
                        ItemId = productId,
                        Quantity = quantity,
                        Operation = "add"
                    }
                },
                new JsonSerializerSettings
                {
                    ContractResolver = new CamelCasePropertyNamesContractResolver()
                }

            );
            request.AddParameter("application/json; charset=utf-8", input, ParameterType.RequestBody);
            var response = this.restClient.Execute(request);
            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                return JsonConvert.DeserializeObject<Cart>(response.Content);
            }
            throw new Exception("Could not create cart. Error Code: " + response.StatusCode + ", Error Message: " + response.ErrorMessage);
        }

        public ICart RemoveProductFromCart(ICart cart, string productId)
        {
            var request = new RestRequest("/cart/" + cart.Id, Method.PUT);
            var input = JsonConvert.SerializeObject(
                new List<InputCartItem>()
                {
                    new InputCartItem
                    {
                        ItemId = productId,
                        Operation = "remove"
                    }
                },
                new JsonSerializerSettings
                {
                    ContractResolver = new CamelCasePropertyNamesContractResolver()
                }

            );
            request.AddParameter("application/json; charset=utf-8", input, ParameterType.RequestBody);
            var response = this.restClient.Execute(request);
            if (response.StatusCode == System.Net.HttpStatusCode.OK)
            {
                return JsonConvert.DeserializeObject<Cart>(response.Content);
            }
            throw new Exception("Could not update cart. Error Code: " + response.StatusCode + ", Error Message: " + response.ErrorMessage);
        }
    }
}
