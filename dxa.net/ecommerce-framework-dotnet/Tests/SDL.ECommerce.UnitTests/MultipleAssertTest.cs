namespace SDL.ECommerce.UnitTests
{
    using System;

    using Ploeh.AutoFixture;

    public abstract class MultipleAssertTest<TParent> where TParent : Test
    {
        protected TParent Parent { get; }

        public IFixture Fixture { get; }

        protected MultipleAssertTest()
        {
            this.Parent = Activator.CreateInstance<TParent>();

            this.Fixture = this.Parent.Fixture;
        }
    }
}