namespace SDL.ECommerce.Rest
{
    using RestSharp;

    public class RestClientFactory
    {
        public virtual IRestClient CreateClient(string endpointAddress, string locale, string apiVersion = "v1")
        {
            return new RestClient(string.Format("{0}/rest/{1}/{2}", endpointAddress, apiVersion, locale));
        }
    }
}