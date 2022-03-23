using System;

using Xunit;

using SDL.ECommerce.Api;

namespace SDL.ECommerce.UnitTests.Api
{
    using System.Linq;

    public class FacetParameter_Test
    {
        [InlineData("10", FacetParameter.ParameterType.MULTISELECT, "10")]
        [InlineData("10|20|30", FacetParameter.ParameterType.MULTISELECT, "10", "20", "30")]
        [InlineData(">10", FacetParameter.ParameterType.GREATER_THAN, "10")]
        [InlineData("<10", FacetParameter.ParameterType.LESS_THAN, "10")]
        [InlineData("10-20", FacetParameter.ParameterType.RANGE, "10","20")]
        [InlineData("blue", FacetParameter.ParameterType.MULTISELECT, "blue")]
        [InlineData("blue|red", FacetParameter.ParameterType.MULTISELECT, "blue", "red")]
        [Theory]
        public void FacetParameterShouldBeMapped(string facetValue, FacetParameter.ParameterType expectedType, params string[] expectedValue)
        {
            var facetParameter = new FacetParameter("testParam", facetValue);
            
            Assert.Equal(expectedValue.ToList(), facetParameter.Values);
            Assert.Equal(expectedType, facetParameter.Type);
        }

        [InlineData("color_val", "80", FacetParameter.ParameterType.SINGLEVALUE, "color", "80")]
        [InlineData("color_val2", "80", FacetParameter.ParameterType.MULTISELECT, "color_val2", "80")]
        [Theory]
        public void WhenFacetNameEndsWith_val_ThenFacetTypeShouldBeSingleValue(string facetName, string facetValue, FacetParameter.ParameterType expectedType, string expectedName, params string[] expectedValue)
        {
            var facetParameter = new FacetParameter(facetName, facetValue);

            Assert.Equal(expectedValue, facetParameter.Values);
            Assert.Equal(expectedType, facetParameter.Type);
        }

        [Fact]
        public void TestFacetParameter()
        {
            var facet = new FacetParameter("brand", "adidas|dkny");
            Console.WriteLine("Facet: " + facet.Name + ", type: " + facet.Type);
            Console.WriteLine("Contains 'adidas': " + facet.ContainsValue("adidas"));
        }
    }
}