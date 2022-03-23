namespace SDL.ECommerce.UnitTests.Fakes
{
    using System.Collections.Generic;
    using ECommerce.Api.Model;

    public class FakeCategory : ICategory
    {
        public FakeCategory(string id = null)
        {
            Id = id;
        }

        public string Id { get; set; }

        public string Name { get; set; }

        public ICategory Parent { get; set; }

        public IList<ICategory> Categories { get; set; }

        public string PathName { get; set; }

        public string SanitizedPathName { get; set; }
    }
}