namespace SDL.ECommerce.UnitTests
{
    using Ploeh.AutoFixture;
    using Ploeh.AutoFixture.AutoNSubstitute;

    using SDL.ECommerce.UnitTests.Customizations;

    public abstract class Test
    {
        public IFixture Fixture { get; }

        protected Test()
        {
            Fixture = new Fixture().Customize(new AutoNSubstituteCustomization())
                                   .Customize(new MvcCustomization());
        }
    }

    public abstract class Test<T> : Test
    {
        private T _systemUnderTest;

        public T SystemUnderTest
        {
            get
            {
                if (_systemUnderTest == null)
                {
                    _systemUnderTest = Fixture.Freeze<T>();
                }

                return _systemUnderTest;
            }
        }
    }
}