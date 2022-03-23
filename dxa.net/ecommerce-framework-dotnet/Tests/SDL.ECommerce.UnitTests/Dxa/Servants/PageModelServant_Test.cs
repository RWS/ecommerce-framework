namespace SDL.ECommerce.UnitTests.Dxa.Servants
{
    using System;
    using System.Linq;
    using System.Web;

    using FakeHttpContext;

    using NSubstitute;
    using NSubstitute.ExceptionExtensions;

    using Ploeh.AutoFixture;

    using Sdl.Web.Common;
    using Sdl.Web.Common.Configuration;
    using Sdl.Web.Common.Interfaces;
    using Sdl.Web.Common.Models;

    using SDL.ECommerce.Api.Model;
    using SDL.ECommerce.DXA.Servants;

    using Xunit;

    using Query = SDL.ECommerce.Api.Model.Query;

    public class PageModelServant_Test : Test<PageModelServant>
    {
        private readonly Localization _localization;

        public PageModelServant_Test()
        {
            _localization = Fixture.Create<Localization>();
        }

        public class WhenResolvingPageTemplateResultsInNoMatch : MultipleAssertTest<PageModelServant_Test>
        {
            private readonly PageModel _result;

            public WhenResolvingPageTemplateResultsInNoMatch()
            {
                var paths = Fixture.CreateMany<string>(3);

                var contentProvider = Substitute.For<IContentProvider>();

                contentProvider.GetPageModel(Arg.Any<string>(), Parent._localization)
                               .Throws(new DxaItemNotFoundException(Fixture.Create<string>()));

                _result = Parent.SystemUnderTest.ResolveTemplatePage(paths, contentProvider);
            }

            [Fact]
            public void ThenReturnNull()
            {
                Assert.Null(_result);
            }
        }

        public class WhenResolvingPageTemplateResultsInMatch : MultipleAssertTest<PageModelServant_Test>
        {
            private readonly PageModel _result;

            private readonly PageModel _model;

            public WhenResolvingPageTemplateResultsInMatch()
            {
                var paths = Fixture.CreateMany<string>(3).ToList();

                _model = Fixture.Create<PageModel>();

                var contentProvider = Substitute.For<IContentProvider>();

                contentProvider.GetPageModel(paths.Skip(1)
                                                  .First(), Parent._localization)
                               .Returns(_model);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    _result = Parent.SystemUnderTest.ResolveTemplatePage(paths, contentProvider);
                }
            }

            [Fact]
            public void ThenReturnNull()
            {
                Assert.Equal(_model, _result);
            }
        }

        public class WhenSettingPageTemplate : MultipleAssertTest<PageModelServant_Test>
        {
            [Fact]
            public void ThenTheModelShouldBeSet()
            {
                var model = Fixture.Create<PageModel>();

                using (new FakeHttpContext())
                {
                    Parent.SystemUnderTest.SetTemplatePage(model);

                    Assert.Equal(model, HttpContext.Current.Items["PageModel"]);
                }
            }
        }

        public class WhenGettingQueryContributions : MultipleAssertTest<PageModelServant_Test>
        {
            private readonly Query _query;

            public WhenGettingQueryContributions()
            {
                _query = Fixture.Create<Query>();
            }
            
            [InlineData("Header", 0)]
            [InlineData("Footer", 0)]
            [InlineData(null, 1)]
            [Theory]
            public void AndRegionArePassed(string regionName, int receiveCount)
            {
                var setup = SetupEntity(regionName ?? Fixture.Create<string>());

                Parent.SystemUnderTest.GetQueryContributions(setup.Item1, _query);

                setup.Item2.Received(receiveCount).ContributeToQuery(_query);
            }

            [InlineData("Header", 0, null, 1)]
            [InlineData(null, 1, "Header", 0)]
            [InlineData(null, 1, null, 1)]
            [Theory]
            public void AndMultipleIQueryContributorArePassed(string firstRegionName, int firstRegionReceiveCount, string secondRegionName, int secondRegionReceiveCount)
            {
                var setup = SetupEntity(firstRegionName ?? Fixture.Create<string>());

                var firstContributor = setup.Item2;

                setup = SetupEntity(secondRegionName ?? Fixture.Create<string>(), setup.Item1);

                Parent.SystemUnderTest.GetQueryContributions(setup.Item1, _query);

                firstContributor.Received(firstRegionReceiveCount).ContributeToQuery(_query);

                setup.Item2.Received(secondRegionReceiveCount).ContributeToQuery(_query);
            }

            private Tuple<PageModel, IQueryContributor> SetupEntity(string regionName, PageModel pageModel = null)
            {
                var region = new RegionModel(regionName);

                var contributor = Substitute.For<EntityModel, IQueryContributor>();
                
                region.Entities.Add(contributor);

                var model = pageModel ?? new PageModel(Fixture.Create<string>());

                model.Regions.Add(region);

                return new Tuple<PageModel, IQueryContributor>(model, contributor as IQueryContributor);
            }
        }

        public class WhenGettingNotFoundPageDataAndPageExists : MultipleAssertTest<PageModelServant_Test>
        {
            private readonly PageModel _result;

            private readonly PageModel _model;

            public WhenGettingNotFoundPageDataAndPageExists()
            {
                _model = Fixture.Create<PageModel>();

                var contentProvider = Fixture.Freeze<IContentProvider>();

                contentProvider.GetPageModel(Arg.Any<string>(), Arg.Any<Localization>())
                               .Returns(_model);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    _result = Parent.SystemUnderTest.GetNotFoundPageModel(contentProvider);
                }
            }

            [Fact]
            public void ThenTheModelShouldBeReturned()
            {
                Assert.Equal(_model, _result);
            }

            [Fact]
            public void ThenTheUrlShouldBeLocalized()
            {
                Fixture.GetStub<IContentProvider>()
                       .Received(1)
                       .GetPageModel($"{Parent._localization.Path}/error-404", Parent._localization);
            }
        }

        public class WhenGettingNotFoundPageDataAndPageDoesNotExists : MultipleAssertTest<PageModelServant_Test>
        {
            private readonly DxaItemNotFoundException _exception;

            private readonly Exception _result;

            public WhenGettingNotFoundPageDataAndPageDoesNotExists()
            {
                var contentProvider = Fixture.Freeze<IContentProvider>();

                _exception = Fixture.Create<DxaItemNotFoundException>();

                contentProvider.GetPageModel(Arg.Any<string>(), Arg.Any<Localization>())
                               .Throws(_exception);

                using (new FakeHttpContext())
                {
                    HttpContext.Current.Items.Add("Localization", Parent._localization);

                    try
                    {
                        Parent.SystemUnderTest.GetNotFoundPageModel(contentProvider);
                    }
                    catch (Exception ex)
                    {
                        _result = ex;
                    }
                }
            }

            [Fact]
            public void ThenAHttpExceptionShouldBeThrown()
            {
                Assert.IsType<HttpException>(_result);
            }

            [Fact]
            public void ThenExceptionCodeShouldBe404()
            {
                Assert.Equal(404, ((HttpException)_result).GetHttpCode());
            }

            [Fact]
            public void ThenTheExcepotionMessageShouldBePassed()
            {
                Assert.Equal(_exception.Message, _result.Message);
            }
        }
    }
}