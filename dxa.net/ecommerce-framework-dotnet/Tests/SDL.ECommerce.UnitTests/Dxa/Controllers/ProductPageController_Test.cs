namespace SDL.ECommerce.UnitTests.Dxa.Controllers
{
    using System.Collections;
    using System.Collections.Generic;
    using System.Collections.Specialized;
    using System.Linq;
    using System.Web;
    using System.Web.Mvc;

    using FakeHttpContext;

    using NSubstitute;

    using Ploeh.AutoFixture;

    using Sdl.Web.Common.Configuration;
    using Sdl.Web.Common.Interfaces;
    using Sdl.Web.Common.Models;

    using SDL.ECommerce.Api;
    using SDL.ECommerce.DXA.Controllers;
    using SDL.ECommerce.DXA.Servants;

    using Xunit;

    public class ProductPageController_Test : Test<ProductPageController>
    {
        private readonly Localization _localization;

        private readonly Url _url;

        public ProductPageController_Test()
        {
            _localization = Fixture.Create<Localization>();

            _url = Fixture.Create<Url>();
        }

        public class WhenCallingProductPageWithValidUrlWithoutQueryString : MultipleAssertTest<ProductPageController_Test>
        {
            private readonly PageModel _pageModel;

            private readonly IDictionary _httpContextItems;

            private readonly ViewResult _result;

            private readonly IProduct _product;

            public WhenCallingProductPageWithValidUrlWithoutQueryString()
            {
                _pageModel = Fixture.Create<PageModel>();

                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(_pageModel);

                _product = Fixture.Create<IProduct>();

                _product.Name.Returns(Fixture.Create<string>());

                Fixture.Freeze<IECommerceClient>()
                       .DetailService.GetDetail(Parent._url.OneLevel.Replace("/", string.Empty))
                       .Returns(_product);
                
                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection());

                        _result = Parent.SystemUnderTest.ProductPage(Parent._url.OneLevel) as ViewResult;
                    }

                    _httpContextItems = HttpContext.Current.Items;
                }
            }

            [Fact]
            public void TheResolvedTemplatePageShouldBeReturned()
            {
                Assert.Equal(_pageModel, _result.Model);
            }

            [Fact]
            public void TemplateTitleSholdBeModelName()
            {
                Assert.Equal(_product.Name, ((PageModel)_result.Model).Title);
            }
            
            [Fact]
            public void TheProductShouldBeSetInTheContext()
            {
                Assert.Equal(_product, _httpContextItems["ECOM-Product"]);
            }

            [Fact]
            public void TheUrlPrrefixShouldBeSetInTheContext()
            {
                Assert.Equal($"{Parent._localization.Path}/c", _httpContextItems["ECOM-UrlPrefix"]);
            }

            [Fact]
            public void ControllerRouteValueShouldBePage()
            {
                Assert.Equal("Page", Parent.SystemUnderTest.RouteData.Values["Controller"]);
            }
        }

        public class WhenCallingProductPageWithOneLevelUrl : MultipleAssertTest<ProductPageController_Test>
        {
            public WhenCallingProductPageWithOneLevelUrl()
            {
                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection());

                        Parent.SystemUnderTest.ProductPage(Parent._url.OneLevel);
                    }
                }
            }

            [Fact]
            public void ThenACallToPathServantWithEmptySeoIdShouldBeMade()
            {
                Fixture.GetStub<IPathServant>()
                       .Received(1)
                       .GetSearchPath(null, Arg.Any<IProduct>());
            }
        }

        public class WhenCallingProductPageWithTwoLevelUrl : MultipleAssertTest<ProductPageController_Test>
        {
            private readonly string[] _urlParts;

            public WhenCallingProductPageWithTwoLevelUrl()
            {
                _urlParts = Parent._url.Parts(Parent._url.TwoLevels);

                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection());

                        Parent.SystemUnderTest.ProductPage(Parent._url.TwoLevels);
                    }
                }
            }

            [Fact]
            public void ThenACallToPathServantWithFirstUrlPartShouldBeMade()
            {
                Fixture.GetStub<IPathServant>()
                       .Received(1)
                       .GetSearchPath(_urlParts[0], Arg.Any<IProduct>());
            }

            [Fact]
            public void ThenACallToGetProductDetailWithTheSecondUrlPartShouldBeMade()
            {
                Fixture.GetStub<IECommerceClient>()
                       .DetailService.Received(1)
                       .GetDetail(_urlParts[1]);
            }
        }

        public class WhenCallingProductPageWithEmptyUrl : MultipleAssertTest<ProductPageController_Test>
        {
            private readonly ActionResult _result;

            private readonly PageModel _errorModel;

            public WhenCallingProductPageWithEmptyUrl()
            {
                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                _errorModel = Fixture.Create<PageModel>();

                Fixture.Freeze<IPageModelServant>()
                       .GetNotFoundPageModel(Arg.Any<IContentProvider>())
                       .Returns(_errorModel);

                using (new DependencyTestProvider(Fixture))
                {
                    Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection());

                    _result = Parent.SystemUnderTest.ProductPage(string.Empty);
                }
            }

            [Fact]
            public void TheStatusCodeIs404()
            {
                Assert.Equal(404, Parent.SystemUnderTest.Response.StatusCode);
            }

            [Fact]
            public void ControllerRouteValueShouldBePage()
            {
                Assert.Equal("Page", Parent.SystemUnderTest.RouteData.Values["Controller"]);
            }

            [Fact]
            public void TheResultIsOfTypeViewResult()
            {
                Assert.IsType<ViewResult>(_result);
            }
            
            [Fact]
            public void TheErrorMethodIsReturned()
            {
                Assert.Equal(_errorModel, ((ViewResult)_result).Model);
            }
        }

        public class WhenCallingProductWithQueryStringParameters : MultipleAssertTest<ProductPageController_Test>
        {
            private readonly KeyValuePair<string, string>[] _queryStringParameters;

            private IDictionary<string, string>_attributesUsed;

            public WhenCallingProductWithQueryStringParameters()
            {
                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                Fixture.Freeze<IECommerceClient>()
                       .DetailService.GetDetail(Arg.Any<string>(), Arg.Do<IDictionary<string, string>>(dictionary => _attributesUsed = dictionary));

                _queryStringParameters = Fixture.CreateMany<KeyValuePair<string, string>>(2)
                                .ToArray();

                var collection = new NameValueCollection();

                foreach (var queryStringParameter in _queryStringParameters)
                {
                    collection.Add(queryStringParameter.Key, queryStringParameter.Value);
                }

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        Parent.SystemUnderTest.Request.QueryString.Returns(collection);

                        Parent.SystemUnderTest.ProductPage(Parent._url.OneLevel);
                    }
                }
            }

            [Fact]
            public void TheQueryStringParametersIsPassedWhenGettingAProduct()
            {
                Assert.Equal(_queryStringParameters, _attributesUsed);
            }

            [Fact]
            public void ThenNoAttemtToGetProductWithoutVariantAttributesShouldBeMade()
            {
                Fixture.GetStub<IECommerceClient>()
                       .DetailService.GetDetail(Parent._url.OneLevel)
                       .DidNotReceive();
            }
        }

        public class WhenCallingProductWithQueryStringParametersButNoProductIsFound : MultipleAssertTest<ProductPageController_Test>
        {
            public WhenCallingProductWithQueryStringParametersButNoProductIsFound()
            {
                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                Fixture.Freeze<IECommerceClient>()
                       .DetailService.GetDetail(Arg.Any<string>(), Arg.Any<IDictionary<string, string>>())
                       .Returns((IProduct)null);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection());

                        Parent.SystemUnderTest.ProductPage(Parent._url.OneLevel);
                    }
                }
            }
            
            [Fact]
            public void ThenAnAttemtToGetProductAnyVariantAttributesShouldBeMade()
            {
                Fixture.GetStub<IECommerceClient>()
                       .DetailService.Received(1)
                       .GetDetail(Parent._url.Parts(Parent._url.OneLevel)[0]);
            }
        }

        public class WhenCallingProductWithAValidUrlAndNoProductExist : MultipleAssertTest<ProductPageController_Test>
        {
            private readonly ActionResult _result;

            private readonly PageModel _errorModel;

            public WhenCallingProductWithAValidUrlAndNoProductExist()
            {
                Fixture.Freeze<IECommerceClient>()
                       .DetailService.GetDetail(Arg.Any<string>(), Arg.Any<IDictionary<string, string>>())
                       .Returns((IProduct)null);

                Fixture.Freeze<IECommerceClient>()
                       .DetailService.GetDetail(Arg.Any<string>())
                       .Returns((IProduct)null);

                _errorModel = Fixture.Create<PageModel>();

                Fixture.Freeze<IPageModelServant>()
                       .GetNotFoundPageModel(Arg.Any<IContentProvider>())
                       .Returns(_errorModel);

                using (new DependencyTestProvider(Fixture))
                {
                    Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection { { Fixture.Create<string>(), Fixture.Create<string>() } });

                    _result = Parent.SystemUnderTest.ProductPage(Parent._url.OneLevel);
                }
            }

            [Fact]
            public void TheStatusCodeIs404()
            {
                Assert.Equal(404, Parent.SystemUnderTest.Response.StatusCode);
            }

            [Fact]
            public void ControllerRouteValueShouldBePage()
            {
                Assert.Equal("Page", Parent.SystemUnderTest.RouteData.Values["Controller"]);
            }

            [Fact]
            public void TheResultIsOfTypeViewResult()
            {
                Assert.IsType<ViewResult>(_result);
            }

            [Fact]
            public void TheErrorMethodIsReturned()
            {
                Assert.Equal(_errorModel, ((ViewResult)_result).Model);
            }
        }

        public class WhenCallingProductWithAValidUrlAndNoTemplatePageExist : MultipleAssertTest<ProductPageController_Test>
        {
            private readonly ActionResult _result;

            private readonly PageModel _errorModel;

            public WhenCallingProductWithAValidUrlAndNoTemplatePageExist()
            {
                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns((PageModel)null);

                _errorModel = Fixture.Create<PageModel>();

                Fixture.Freeze<IPageModelServant>()
                       .GetNotFoundPageModel(Arg.Any<IContentProvider>())
                       .Returns(_errorModel);

                using (new DependencyTestProvider(Fixture))
                {
                    Parent.SystemUnderTest.Request.QueryString.Returns(new NameValueCollection { { Fixture.Create<string>(), Fixture.Create<string>() } });

                    _result = Parent.SystemUnderTest.ProductPage(Parent._url.OneLevel);
                }
            }

            [Fact]
            public void TheStatusCodeIs404()
            {
                Assert.Equal(404, Parent.SystemUnderTest.Response.StatusCode);
            }

            [Fact]
            public void ControllerRouteValueShouldBePage()
            {
                Assert.Equal("Page", Parent.SystemUnderTest.RouteData.Values["Controller"]);
            }

            [Fact]
            public void TheResultIsOfTypeViewResult()
            {
                Assert.IsType<ViewResult>(_result);
            }

            [Fact]
            public void TheErrorMethodIsReturned()
            {
                Assert.Equal(_errorModel, ((ViewResult)_result).Model);
            }
        }
    }
}