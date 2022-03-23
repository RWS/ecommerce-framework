namespace SDL.ECommerce.UnitTests.Customizations
{
    using System.Web.Mvc;

    using Ploeh.AutoFixture;

    internal class MvcCustomization : ICustomization
    {
        public void Customize(IFixture fixture)
        {
            fixture.Customize<ControllerContext>(c => c.Without(x => x.DisplayMode));
        }
    }
}