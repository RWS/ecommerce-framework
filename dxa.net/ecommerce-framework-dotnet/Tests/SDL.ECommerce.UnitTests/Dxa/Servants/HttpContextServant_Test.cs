namespace SDL.ECommerce.UnitTests.Dxa.Servants
{
    using System.Collections.Generic;
    using System.Collections.Specialized;
    using System.Linq;
    using System.Web;
    
    using NSubstitute;

    using Ploeh.AutoFixture;

    using SDL.ECommerce.Api;
    using SDL.ECommerce.DXA.Servants;

    using Xunit;

    public class HttpContextServant_Test : Test<HttpContextServant>
    {
        private readonly IEnumerable<KeyValuePair<string, string>> _parameters;

        private readonly HttpContextBase _context;

        public HttpContextServant_Test()
        {
            _parameters = Fixture.CreateMany<KeyValuePair<string, string>>(2);

            _context = Substitute.For<HttpContextBase>();

            var parameters = new NameValueCollection();

            foreach (var keyValuePair in _parameters)
            {
                parameters.Add(keyValuePair.Key, keyValuePair.Value);
            }

            _context.Request.QueryString.Returns(parameters);
        }

        public class WhenGettingFacetParameters : MultipleAssertTest<HttpContextServant_Test>
        {
            private readonly IList<FacetParameter> _result;

            public WhenGettingFacetParameters()
            {
                _result = Parent.SystemUnderTest.GetFacetParametersFromRequest(Parent._context);
            }

            [Fact]
            public void TwoFacetsShouldBeCreated()
            {
                Assert.Equal(2, _result.Count);
            }

            [Fact]
            public void TheFirstParameterShouldBeMapped()
            {
                var parameter = Parent._parameters.First();

                var facet = _result.First();

                Assert.Equal(parameter.Key, facet.Name);
                Assert.Equal(parameter.Value, facet.Values.First());
            }

            [Fact]
            public void TheSecondParameterShouldBeMapped()
            {
                var parameter = Parent._parameters.Last();

                var facet = _result.Last();

                Assert.Equal(parameter.Key, facet.Name);
                Assert.Equal(parameter.Value, facet.Values.First());
            }
        }

        public class WhenGettingFacetParametersAndHasQAsParameter : MultipleAssertTest<HttpContextServant_Test>
        {
            private readonly IList<FacetParameter> _result;

            public WhenGettingFacetParametersAndHasQAsParameter()
            {
                Parent._context.Request.QueryString.Add("q", Fixture.Create<string>());

                _result = Parent.SystemUnderTest.GetFacetParametersFromRequest(Parent._context);
            }

            [Fact]
            public void TwoFacetsShouldBeCreated()
            {
                Assert.Equal(2, _result.Count);
            }

            [Fact]
            public void QShouldNotBeAFacetName()
            {
                Assert.False(_result.Any(f => f.Name == "q"));
            }
        }

        public class WhenGettingFacetParametersAndHasStartIndexAsParameter : MultipleAssertTest<HttpContextServant_Test>
        {
            private readonly IList<FacetParameter> _result;

            public WhenGettingFacetParametersAndHasStartIndexAsParameter()
            {
                Parent._context.Request.QueryString.Add("startIndex", Fixture.Create<string>());

                _result = Parent.SystemUnderTest.GetFacetParametersFromRequest(Parent._context);
            }

            [Fact]
            public void TwoFacetsShouldBeCreated()
            {
                Assert.Equal(2, _result.Count);
            }

            [Fact]
            public void QShouldNotBeAFacetName()
            {
                Assert.False(_result.Any(f => f.Name == "startIndex"));
            }
        }

        public class WhenGettingStartIndex : MultipleAssertTest<HttpContextServant_Test>
        {
            [Fact]
            public void WhenNoStartIndexExist_Then0ShouldBeReturned()
            {
                var result = Parent.SystemUnderTest.GetStartIndex(Parent._context);

                Assert.Equal(0, result);
            }

            [Fact]
            public void WhenStartIndexIsInteger_ThenTheGivenIntegerShouldBeReturned()
            {
                var number = Fixture.Create<int>();

                Parent._context.Request.QueryString.Add("startIndex", number.ToString());

                var result = Parent.SystemUnderTest.GetStartIndex(Parent._context);

                Assert.Equal(number, result);
            }

            [Fact]
            public void WhenStartIndexIsNotAnInteger_Then0ShouldBeReturned()
            {
                Parent._context.Request.QueryString.Add("startIndex", Fixture.Create<string>());

                var result = Parent.SystemUnderTest.GetStartIndex(Parent._context);

                Assert.Equal(0, result);
            }
        }
    }
}