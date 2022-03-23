namespace SDL.ECommerce.UnitTests.Dxa.Controllers
{
    using System.Collections;
    using System.Collections.Generic;
    using System.Web;
    using System.Web.Mvc;

    using FakeHttpContext;

    using NSubstitute;

    using Ploeh.AutoFixture;

    using Sdl.Web.Common.Configuration;
    using Sdl.Web.Common.Interfaces;
    using Sdl.Web.Common.Models;

    using SDL.ECommerce.Api;
    using SDL.ECommerce.Api.Model;
    using SDL.ECommerce.DXA.Controllers;
    using SDL.ECommerce.DXA.Servants;

    using Xunit;
    
    public class CategoryPageController_Test : Test<CategoryPageController>
    {
        private readonly string _url;

        private readonly Localization _localization;

        public CategoryPageController_Test()
        {
            _url = Fixture.Create<string>();

            _localization = Fixture.Create<Localization>();
        }

        public class WhenCallingCategoryPageWithValidUrl : MultipleAssertTest<CategoryPageController_Test>
        {
            private readonly PageModel _pageModel;

            private readonly IDictionary _httpContextItems;

            private readonly PageModel _resultModel;

            private readonly ICategory _category;

            private readonly IList<FacetParameter> _parameters;
            
            public WhenCallingCategoryPageWithValidUrl()
            {
                Fixture.Freeze<IECommerceClient>()
                       .QueryService.Query(Arg.Any<ECommerce.Api.Model.Query>())
                       .RedirectLocation.Returns((ILocation)null);

                _pageModel = Fixture.Create<PageModel>();

                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(_pageModel);

                _category = Fixture.Create<ICategory>();

                _category.Name.Returns(Fixture.Create<string>());

                Fixture.GetStub<IECommerceClient>()
                       .CategoryService.GetCategoryByPath(Parent._url)
                       .Returns(_category);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        var result = Parent.SystemUnderTest.CategoryPage(Parent._url);

                        _resultModel = ((ViewResult)result).Model as PageModel;
                    }

                    _httpContextItems = HttpContext.Current.Items;
                }

                _parameters = Fixture.GetStub<IHttpContextServant>()
                                        .GetFacetParametersFromRequest(Arg.Any<HttpContextBase>());
            }

            [Fact]
            public void CategoryWillBeSetInTheHttpContextCurrentQueryItem()
            {
                Assert.Equal(_category, ((ECommerce.Api.Model.Query)_httpContextItems["ECOM-CurrentQuery"]).Category);
            }

            [Fact]
            public void FacetsWillBeSetInTheHttpContextCurrentQueryItemItem()
            {
                Assert.Equal(_parameters, ((ECommerce.Api.Model.Query)_httpContextItems["ECOM-CurrentQuery"]).Facets);
            }

            [Fact]
            public void StartIndexWillBeSetInTheHttpContextCurrentQueryItemItem()
            {
                var startIndex = Fixture.GetStub<IHttpContextServant>()
                                        .GetStartIndex(Arg.Any<HttpContextBase>());

                Assert.Equal(startIndex, ((ECommerce.Api.Model.Query)_httpContextItems["ECOM-CurrentQuery"]).StartIndex);
            }

            [Fact]
            public void SearchResultWillBeSetInTheHttpContext()
            {
                var query = Fixture.GetStub<IECommerceClient>()
                                   .QueryService.Query(Arg.Any<ECommerce.Api.Model.Query>());

                Assert.Equal(query, _httpContextItems["ECOM-QueryResult"]);
            }

            [Fact]
            public void UrlPrefixWillBeSetInTheHttpContext()
            {
                var url = $"{Parent._localization.Path}/c";

                Assert.Equal(url, _httpContextItems["ECOM-UrlPrefix"]);
            }

            [Fact]
            public void FacetsWillBeSetInTheHttpContext()
            {
                Assert.Equal(_parameters, _httpContextItems["ECOM-Facets"]);
            }

            [Fact]
            public void CategoryWillBeSetInTheHttpContext()
            {
                Assert.Equal(_category, _httpContextItems["ECOM-Category"]);
            }

            [Fact]
            public void CategoryUrlShouldBeUsedWhenGettingSearchPath()
            {
                Fixture.GetStub<IPathServant>()
                       .Received(1)
                       .GetSearchPath(Parent._url, Arg.Any<ICategory>());
            }

            [Fact]
            public void CategoryShouldBeUsedWhenGettingSearchPath()
            {
                Fixture.GetStub<IPathServant>()
                       .Received(1)
                       .GetSearchPath(Arg.Any<string>(), _category);
            }

            [Fact]
            public void CategoryNameIsSetAsModelTemplate()
            {
                Assert.Equal(_category.Name, _resultModel.Title);
            }

            [Fact]
            public void TheModelIsSet()
            {
                Fixture.GetStub<IPageModelServant>()
                       .Received(1)
                       .SetTemplatePage(_pageModel);
            }

            [Fact]
            public void QueryContributorsShouldBeSet()
            {
                Fixture.GetStub<IPageModelServant>()
                       .Received(1)
                       .GetQueryContributions(Arg.Is<PageModel>(model => model.Title == _pageModel.Title), Arg.Is<ECommerce.Api.Model.Query>(model => model.Category == _category));
            }

            [Fact]
            public void QueryIsCalledWithTheCreatedQuery()
            {
                Fixture.GetStub<IECommerceClient>()
                       .QueryService.Received(1)
                       .Query(Arg.Is<ECommerce.Api.Model.Query>(model => model.Category == _category));
            }

            [Fact]
            public void ControllerRouteValueShouldBePage()
            {
                Assert.Equal("Page", Parent.SystemUnderTest.RouteData.Values["Controller"]);
            }
        }

        public class WhenCallingCategoryPageWithEmptyUrl : MultipleAssertTest<CategoryPageController_Test>
        {
            public WhenCallingCategoryPageWithEmptyUrl()
            {
                Fixture.Freeze<IECommerceClient>()
                       .QueryService.Query(Arg.Any<ECommerce.Api.Model.Query>())
                       .RedirectLocation.Returns((ILocation)null);

                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        Parent.SystemUnderTest.CategoryPage(null);
                    }
                }
            }

            [Fact]
            public void TheCategoryUrlShouldBeChangedToSingleSlash()
            {
                Fixture.GetStub<IPathServant>()
                       .Received(1)
                       .GetSearchPath("/", Arg.Any<ICategory>());
            }
        }

        public class WhenCallingCategoryPageAndSearchResultIsRedirect : MultipleAssertTest<CategoryPageController_Test>
        {
            private readonly ActionResult _result;

            public WhenCallingCategoryPageAndSearchResultIsRedirect()
            {
                Fixture.Freeze<IPageModelServant>()
                       .ResolveTemplatePage(Arg.Any<IEnumerable<string>>(), Arg.Any<IContentProvider>())
                       .Returns(Fixture.Create<PageModel>());

                Fixture.Freeze<IECommerceLinkResolver>()
                       .GetLocationLink(Fixture.Freeze<IECommerceClient>()
                                               .QueryService.Query(Arg.Any<ECommerce.Api.Model.Query>())
                                               .RedirectLocation)
                       .Returns("http://localhost:1234");

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        _result = Parent.SystemUnderTest.CategoryPage(Fixture.Create<string>());
                    }
                }
            }

            [Fact]
            public void TheResultIsARedirectResult()
            {
                Assert.IsType<RedirectResult>(_result);
            }

            [Fact]
            public void TheRedirectUrlShouldBeTheOneSetByTheLinkResolver()
            {
                Assert.Equal("http://localhost:1234", ((RedirectResult)_result).Url);
            }
        }

        public class WhenCallingCategoryPageAndCategoryDoNotExist : MultipleAssertTest<CategoryPageController_Test>
        {
            private readonly ActionResult _result;
            
            private readonly PageModel _errorModel;

            public WhenCallingCategoryPageAndCategoryDoNotExist()
            {
                Fixture.Freeze<IECommerceClient>()
                       .CategoryService.GetCategoryByPath(Arg.Any<string>())
                       .Returns((ICategory)null);

                _errorModel = Fixture.Create<PageModel>();

                Fixture.Freeze<IPageModelServant>()
                       .GetNotFoundPageModel(Arg.Any<IContentProvider>())
                       .Returns(_errorModel);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    using (new DependencyTestProvider(Fixture))
                    {
                        _result = Parent.SystemUnderTest.CategoryPage(Fixture.Create<string>());
                    }
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