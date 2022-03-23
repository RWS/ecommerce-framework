namespace SDL.ECommerce.UnitTests
{
    using System;
    using System.ComponentModel.DataAnnotations;

    public class Url
    {
        [RegularExpression(@"^http\://localhost:[0-9]{4}$")]
        public string Localhost { get; set; }

        public string NoLevels => "/";

        [RegularExpression(@"^/L1-[a-z]{4,6}$")]
        public string OneLevel { get; set; }

        [RegularExpression(@"^/L1-[a-z]{4,6}/L2-[a-z]{4,6}$")]
        public string TwoLevels { get; set; }

        public string[] Parts(string url)
        {
            return url.Split(new [] { '/' }, StringSplitOptions.RemoveEmptyEntries);
        }
    }
}