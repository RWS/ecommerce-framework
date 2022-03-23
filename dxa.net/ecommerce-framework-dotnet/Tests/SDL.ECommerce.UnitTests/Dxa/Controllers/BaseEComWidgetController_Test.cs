using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Mvc;
using NSubstitute;
using Ploeh.AutoFixture;
using Sdl.Web.Common.Models;
using SDL.ECommerce.Api;
using SDL.ECommerce.Api.Model;
using SDL.ECommerce.Api.Service;
using SDL.ECommerce.DXA.Models;
using SDL.ECommerce.Rest.Service;
using SDL.ECommerce.UnitTests;
using SDL.ECommerce.UnitTests.Dxa.Fakes;
using SDL.ECommerce.UnitTests.Fakes;
using Xunit;
using Query = SDL.ECommerce.Api.Model.Query;

public class BaseEComWidgetController_Test : Test<FakeBaseEcomWidgetController>
{
    [Fact]
    public void WhenInvokingProductLister_ThenTotalCountShouldBeMappedToNavigationData()
    {
        //Arrange
        var expectedTotalCount = Fixture.Create<int>();
        var expectedViewSize = 40;

        var queryService = Fixture.Freeze<IProductQueryService>();
        queryService.Query(Arg.Any<Query>()).TotalCount.Returns(expectedTotalCount);
        queryService.Query(Arg.Any<Query>()).ViewSize.Returns(expectedViewSize);

        var categoryService = Fixture.Freeze<IProductCategoryService>();
        categoryService.GetCategoryByPath(Arg.Any<string>()).Returns((ICategory)null);

        var eCommerceClient = new FakeECommerceClient
        {
            CategoryService = categoryService,
            QueryService = queryService
        };
        
        Fixture.Register<IECommerceClient>(() => eCommerceClient);

        var entity = Fixture.Create<ProductListerWidget>();

        //Act
        ViewResult result;
        using (new DependencyTestProvider(Fixture))
        {
            result = SystemUnderTest.ProductLister(entity) as ViewResult;
        }

        //Assert
        var model = (ProductListerWidget)result?.Model;
        Assert.Equal(model?.NavigationData.TotalCount, expectedTotalCount);
    }

    [Fact]
    public void WhenPassingSingleCategoryReferencesWithCategoryId_ThenTheCategoryIdShouldBePassedToTheRestCall()
    {
        // Arrange
        var restClient = SetupDependencies(new FakeRestResponse());

        var entity = new ProductListerWidget
        {
            MvcData = new MvcData("MyView"),
            CategoryReferences = new List<ECommerceCategoryReference>
            {
                new ECommerceCategoryReference
                {
                    CategoryId = "cat123"
                }
            }
        };

        // Act
        using (new DependencyTestProvider(Fixture))
        {
            SystemUnderTest.ProductLister(entity);
        }

        // Assert
        Assert.Equal(1, restClient.MadeRequests.Count); // Only one request should be made.

        var firstQuery = restClient.MadeRequests.First();

        var categoryIdsParameter = firstQuery.Parameters.Single(c => c.Name.Equals("categoryId", StringComparison.InvariantCulture));

        Assert.Equal("cat123", categoryIdsParameter.Value);
    }

    [Fact]
    public void WhenPassingMultipleCategoryReferencesWithCategoryId_ThenTheCategoryIdsShouldBePassedToTheRestCall()
    {
        // Arrange
        var restClient = SetupDependencies(new FakeRestResponse());

        var entity = new ProductListerWidget
        {
            MvcData = new MvcData("MyView"),
            CategoryReferences = new List<ECommerceCategoryReference>
            {
                new ECommerceCategoryReference
                {
                    CategoryId = "cat123"
                },
                new ECommerceCategoryReference
                {
                    CategoryId = "cat456"
                }
            }
        };
        
        // Act
        using (new DependencyTestProvider(Fixture))
        {
            SystemUnderTest.ProductLister(entity);
        }

        // Assert
        Assert.Equal(1, restClient.MadeRequests.Count); // Only one request should be made.
        
        var firstQuery = restClient.MadeRequests.First();

        var categoryIdsParameter = firstQuery.Parameters.Single(c => c.Name.Equals("categoryIds", StringComparison.InvariantCulture));

        Assert.Equal("cat123|cat456", categoryIdsParameter.Value);
    }

    [Fact]
    public void WhenPassingMultipleCategoryReferencesWithCategoryRef_ThenTheCategoryIdsShouldBePassedToTheRestCall()
    {
        // Arrange
        var restClient = SetupDependencies(new FakeRestResponse());

        var entity = new ProductListerWidget
        {
            MvcData = new MvcData("MyView"),
            CategoryReferences = new List<ECommerceCategoryReference>
            {
                new ECommerceCategoryReference
                {
                    CategoryRef = CreateCategoryRef("cat123")
                },
                new ECommerceCategoryReference
                {
                    CategoryRef = CreateCategoryRef("cat456")
                }
            }
        };

        // Act
        using (new DependencyTestProvider(Fixture))
        {
            SystemUnderTest.ProductLister(entity);
        }

        // Assert
        Assert.Equal(1, restClient.MadeRequests.Count); // Only one request should be made.

        var firstQuery = restClient.MadeRequests.First();

        var categoryIdsParameter = firstQuery.Parameters.Single(c => c.Name.Equals("categoryIds", StringComparison.InvariantCulture));

        Assert.Equal("cat123|cat456", categoryIdsParameter.Value);
    }

    [Fact]
    public void WhenPassingMultipleCategoryReferencesWithCategoryPath_ThenTheCategoryIdsShouldBePassedToTheRestCall()
    {
        // Arrange
        var categoryService = Fixture.Freeze<IProductCategoryService>();
        categoryService.GetCategoryByPath("level1/level2").Returns(new FakeCategory("cat123"));
        categoryService.GetCategoryByPath("level1/level2/level3").Returns(new FakeCategory("cat456"));

        var restClient = SetupDependencies(new FakeRestResponse(), categoryService);

        var entity = new ProductListerWidget
        {
            MvcData = new MvcData("MyView"),
            CategoryReferences = new List<ECommerceCategoryReference>
            {
                new ECommerceCategoryReference
                {
                    CategoryPath = "level1/level2"
                },
                new ECommerceCategoryReference
                {
                    CategoryPath= "level1/level2/level3"
                }
            }
        };

        // Act
        using (new DependencyTestProvider(Fixture))
        {
            SystemUnderTest.ProductLister(entity);
        }

        // Assert
        Assert.Equal(1, restClient.MadeRequests.Count); // Only one request should be made.

        var firstQuery = restClient.MadeRequests.First();

        var categoryIdsParameter = firstQuery.Parameters.Single(c => c.Name.Equals("categoryIds", StringComparison.InvariantCulture));

        Assert.Equal("cat123|cat456", categoryIdsParameter.Value);
    }

    [Fact]
    public void WhenPassingMultipleCategoryReferencesWithMixedReferenceTypes_ThenTheCategoryIdsShouldBePassedToTheRestCall()
    {
        // Arrange
        var categoryService = Fixture.Freeze<IProductCategoryService>();
        categoryService.GetCategoryByPath("level1/level2").Returns(new FakeCategory("cat123"));

        var restClient = SetupDependencies(new FakeRestResponse(), categoryService);

        var entity = new ProductListerWidget
        {
            MvcData = new MvcData("MyView"),
            CategoryReferences = new List<ECommerceCategoryReference>
            {
                new ECommerceCategoryReference
                {
                    CategoryPath = "level1/level2"
                },
                new ECommerceCategoryReference
                {
                    CategoryId= "cat456"
                },
                new ECommerceCategoryReference
                {
                    CategoryRef = CreateCategoryRef("cat789")
                }
            }
        };

        // Act
        using (new DependencyTestProvider(Fixture))
        {
            SystemUnderTest.ProductLister(entity);
        }

        // Assert
        Assert.Equal(1, restClient.MadeRequests.Count); // Only one request should be made.

        var firstQuery = restClient.MadeRequests.First();

        var categoryIdsParameter = firstQuery.Parameters.Single(c => c.Name.Equals("categoryIds", StringComparison.InvariantCulture));

        Assert.Equal("cat123|cat456|cat789", categoryIdsParameter.Value);
    }

    private static ECommerceEclItem CreateCategoryRef(string categoryId)
    {
        return new ECommerceEclItem
        {
            EclExternalMetadata = new Dictionary<string, object>
            {
                {"Id", categoryId}
            }
        };
    }

    private FakeRestClient SetupDependencies(
        FakeRestResponse response,
        IProductCategoryService productCategoryService = null)
    {
        var restClient = new FakeRestClient();

        if (response != null)
        {
            restClient.AddToQueue(response);
        }

        var eCommerceClient = new FakeECommerceClient
        {
            QueryService = new ProductQueryService(restClient, new FakeECommerceCacheProvider(), null),
            CategoryService = productCategoryService
        };

        Fixture.Register<IECommerceClient>(() => eCommerceClient);

        return restClient;
    }
}