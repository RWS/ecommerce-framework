namespace SDL.ECommerce.UnitTests.Dxa.Factories
{
    using System;

    using NSubstitute;

    using SDL.ECommerce.Api;
    using SDL.ECommerce.DXA;
    using SDL.ECommerce.DXA.Factories;
    using SDL.ECommerce.DXA.Providers;

    using Xunit;

    public class DependencyFactory_Test : Test
    {
        public class WhenSettingADependency : MultipleAssertTest<DependencyFactory_Test>, IDisposable
        {
            private readonly IECommerceLinkResolver _result;

            private readonly IECommerceLinkResolver _dependency;

            public WhenSettingADependency()
            {
                _dependency = Substitute.For<IECommerceLinkResolver>();

                DependencyResolverProvider.Set(type => type == typeof(IECommerceLinkResolver) ? _dependency : null);

                _result = DependencyFactory.Current.Resolve<IECommerceLinkResolver>();
            }

            [Fact]
            public void ThenThatDepedencyShouldBeResolved()
            {
                Assert.Equal(_dependency, _result);
            }

            public void Dispose()
            {
                DependencyResolverProvider.Reset();
            }
        }

        public class WhenNotSettingADependency : MultipleAssertTest<DependencyFactory_Test>, IDisposable
        {
            private readonly IECommerceLinkResolver _result;

            public WhenNotSettingADependency()
            {
                DependencyResolverProvider.Set(type => null);
                
                _result = DependencyFactory.Current.Resolve<IECommerceLinkResolver>();
            }

            [Fact]
            public void ThenADefaultDepedencyShouldBeResolved()
            {
                Assert.IsType<DXALinkResolver>(_result);
            }

            public void Dispose()
            {
                DependencyResolverProvider.Reset();
            }
        }
    }
}