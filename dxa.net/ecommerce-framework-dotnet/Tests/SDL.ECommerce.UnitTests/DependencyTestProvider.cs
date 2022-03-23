namespace SDL.ECommerce.UnitTests
{
    using System;

    using Ploeh.AutoFixture;
    using Ploeh.AutoFixture.Kernel;
    using SDL.ECommerce.DXA.Providers;

    internal class DependencyTestProvider : IDisposable
    {
        public DependencyTestProvider(IFixture fixture)
        {
            var context = new SpecimenContext(fixture);

            DependencyResolverProvider.Set(type =>
                                               {
                                                   var instance = fixture.Create(type, context);

                                                   Inject(fixture, type, instance);

                                                   return instance;
                                               });
        }
        
        public void Dispose()
        {
            DependencyResolverProvider.Reset();
        }

        private static void Inject(IFixture fixture, Type type, object mockedObject)
        {
            typeof(FixtureRegistrar).GetMethod("Inject")
                                    .MakeGenericMethod(type)
                                    .Invoke(null, new[]
                                                      {
                                                          fixture,
                                                          mockedObject
                                                      });
        }
    }
}