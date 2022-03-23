namespace SDL.ECommerce.UnitTests
{
    using Ploeh.AutoFixture;

    public static class FixtureExtensions
    {
        public static T GetStub<T>(this IFixture fixture)
        {
            return fixture.Create<T>();
        }
    }
}