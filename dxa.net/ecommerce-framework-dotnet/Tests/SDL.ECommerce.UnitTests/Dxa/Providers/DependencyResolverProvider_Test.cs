namespace SDL.ECommerce.UnitTests.Dxa.Providers
{
    using System;

    using Ploeh.AutoFixture;

    using SDL.ECommerce.DXA.Providers;

    using Xunit;

    public class DependencyResolverProvider_Test : Test, IDisposable
    {
        private readonly Func<Type, object> _dependencies;

        public DependencyResolverProvider_Test()
        {
            _dependencies = Fixture.Create<Func<Type, object>>();

            DependencyResolverProvider.Set(_dependencies);
        }

        [Fact]
        public void GettingNowShouldBeSetTime()
        {
            Assert.Equal(_dependencies, DependencyResolverProvider.Dependencies);
        }

        [Fact]
        public void AfterResetTheTimeShouldNotBeTheOneSet()
        {
            var dependencies = Fixture.Create<Func<Type, object>>();

            DependencyResolverProvider.Set(dependencies);

            DependencyResolverProvider.Reset();

            Assert.NotEqual(dependencies, DependencyResolverProvider.Dependencies);
        }

        public void Dispose()
        {
            DependencyResolverProvider.Reset();
        }
    }
}