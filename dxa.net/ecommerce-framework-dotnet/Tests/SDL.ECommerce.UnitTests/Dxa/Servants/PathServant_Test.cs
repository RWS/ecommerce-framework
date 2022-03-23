namespace SDL.ECommerce.UnitTests.Dxa.Servants
{
    using System.Collections.Generic;
    using System.Linq;
    using System.Web;

    using FakeHttpContext;

    using NSubstitute;

    using Ploeh.AutoFixture;

    using Sdl.Web.Common.Configuration;

    using SDL.ECommerce.Api.Model;
    using SDL.ECommerce.DXA.Servants;

    using Xunit;

    public class PathServant_Test : Test<PathServant>
    {
        private readonly Localization _localization;

        private readonly string _url;

        private readonly string _urlPart1;

        private readonly string _urlPart2;

        public PathServant_Test()
        {
            _urlPart1 = Fixture.Create<string>();

            _urlPart2 = Fixture.Create<string>();

            _url = $"{_urlPart1}/{_urlPart2}";

            _localization = Fixture.Create<Localization>();
        }

        public class WhenGettingSearchPathForCategory : MultipleAssertTest<PathServant_Test>
        {
            private readonly ICategory _category;

            private readonly string[] _result;

            public WhenGettingSearchPathForCategory()
            {
                _category = Substitute.For<ICategory>();

                _category.Id.Returns(Fixture.Create<string>());

                _category.Parent.Returns(Fixture.Create<ICategory>());

                _category.Parent.Id.Returns(Fixture.Create<string>());

                _category.Parent.Parent.Returns(default(ICategory));

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    _result = Parent.SystemUnderTest.GetSearchPath(Parent._url, _category)
                                    .ToArray();
                }
            }

            [Fact]
            public void PathWithBothUrlPartsShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{Parent._urlPart1}-{Parent._urlPart2}", _result);
            }

            [Fact]
            public void PathWithFirstUrlPartShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{Parent._urlPart1}", _result);
            }

            [Fact]
            public void PathWithCurrentCategoryIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{_category.Id}", _result);
            }

            [Fact]
            public void PathWithCurrentCategoryParentIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{_category.Parent.Id}", _result);
            }

            [Fact]
            public void PathWithGenericShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}generic", _result);
            }

            [Fact]
            public void PathWithBothUrlPartsShouldBeReturnedAsFirstResult()
            {
                Assert.Equal(_result[0], $"{GetBasePath()}{Parent._urlPart1}-{Parent._urlPart2}");
            }

            [Fact]
            public void PathWithFirstUrlPartShouldBeReturnedAsSecondResult()
            {
                Assert.Equal(_result[1], $"{GetBasePath()}{Parent._urlPart1}");
            }

            [Fact]
            public void PathWithCurrentCategoryIdShouldBeReturnedAsThirdResult()
            {
                Assert.Equal(_result[2], $"{GetBasePath()}{_category.Id}");
            }

            [Fact]
            public void PathWithCurrentCategoryParentIdShouldBeReturnedAsForthResult()
            {
                Assert.Equal(_result[3], $"{GetBasePath()}{_category.Parent.Id}");
            }

            [Fact]
            public void PathWithGenericShouldBeReturnedLast()
            {
                Assert.Equal(_result.Last(), $"{GetBasePath()}generic");
            }

            private string GetBasePath()
            {
                return $"{Parent._localization.Path}/categories/";
            }
        }

        public class WhenGettingSearchPathForProductWithoutCategories : MultipleAssertTest<PathServant_Test>
        {
            private readonly IProduct _product;

            private readonly string[] _result;

            public WhenGettingSearchPathForProductWithoutCategories()
            {
                _product = Fixture.Create<IProduct>();

                _product.Id.Returns(Fixture.Create<string>());

                _product.Categories.Returns((IList<ICategory>)null);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    _result = Parent.SystemUnderTest.GetSearchPath(Parent._url, _product)
                                    .ToArray();
                }
            }

            [Fact]
            public void PathWithProductSeoIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{Parent._url}", _result);
            }

            [Fact]
            public void PathWithProductIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{_product.Id}", _result);
            }

            [Fact]
            public void PathWithGenericShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}generic", _result);
            }

            [Fact]
            public void PathWithProductSeoIdShouldBeReturnedAsFirstResult()
            {
                Assert.Equal(_result[0], $"{GetBasePath()}{Parent._url}");
            }

            [Fact]
            public void PathWithProductIdShouldBeReturnedAsSecondResult()
            {
                Assert.Equal(_result[1], $"{GetBasePath()}{_product.Id}");
            }

            [Fact]
            public void PathWithGenericShouldBeReturnedLast()
            {
                Assert.Equal(_result.Last(), $"{GetBasePath()}generic");
            }

            private string GetBasePath()
            {
                return $"{Parent._localization.Path}/products/";
            }
        }

        public class WhenGettingSearchPathForProductWithCategories : MultipleAssertTest<PathServant_Test>
        {
            private readonly IProduct _product;

            private readonly string[] _result;

            public WhenGettingSearchPathForProductWithCategories()
            {
                _product = Fixture.Create<IProduct>();

                _product.Categories.Returns(new List<ICategory>());

                for (var i = 0; i < 3; i++)
                {
                    var category = Fixture.Create<ICategory>();

                    category.Id.Returns(Fixture.Create<string>());

                    _product.Categories.Add(category);
                }

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    _result = Parent.SystemUnderTest.GetSearchPath(Parent._url, _product)
                                    .ToArray();
                }
            }

            [Fact]
            public void PathWithFirstCategoryIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{_product.Categories[0].Id}", _result);
            }

            [Fact]
            public void PathWithSecondCategoryIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{_product.Categories[1].Id}", _result);
            }

            [Fact]
            public void PathWithThirdCategoryIdShouldBeReturned()
            {
                Assert.Contains($"{GetBasePath()}{_product.Categories[2].Id}", _result);
            }

            [Fact]
            public void PathWithFirstCategoryIdShouldBeReturnedAsThirdResult()
            {
                Assert.Equal(_result[2], $"{GetBasePath()}{_product.Categories[0].Id}");
            }

            [Fact]
            public void PathWithSecondCategoryIdShouldBeReturnedAsForthResult()
            {
                Assert.Equal(_result[3], $"{GetBasePath()}{_product.Categories[1].Id}");
            }

            [Fact]
            public void PathWithThirdCategoryIdShouldBeReturnedAsFifthResult()
            {
                Assert.Equal(_result[4], $"{GetBasePath()}{_product.Categories[2].Id}");
            }
            
            private string GetBasePath()
            {
                return $"{Parent._localization.Path}/products/";
            }
        }

        public class WhenGettingSearchPathForProductWithoutProductSeoId : MultipleAssertTest<PathServant_Test>
        {
            private readonly IProduct _product;

            private readonly string[] _result;

            public WhenGettingSearchPathForProductWithoutProductSeoId()
            {
                _product = Fixture.Create<IProduct>();

                _product.Id.Returns(Fixture.Create<string>());

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    _result = Parent.SystemUnderTest.GetSearchPath(string.Empty, _product)
                                    .ToArray();
                }
            }

            [Fact]
            public void NoPathWithOnlyBasePathShouldBeReturned()
            {
                Assert.DoesNotContain($"{GetBasePath()}", _result);
            }

            private string GetBasePath()
            {
                return $"{Parent._localization.Path}/products/";
            }
        }
    }
}