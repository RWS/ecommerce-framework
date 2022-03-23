namespace SDL.ECommerce.UnitTests.Formatting.Servants
{
    using System.Collections.Generic;
    using ECommerce.Formatting.Servants;
    using Xunit;

    public class SanitizerConfiguration_Test
    {
        private readonly ISanitizerConfiguration _defaultSanitizerConfiguration;
        private readonly int _customMaxInputLength;
        private readonly bool _customReturnOriginalCharacterIfMapNotFound;
        private readonly Dictionary<string, string> _customerReplacementCharacterMap;
        private readonly SanitizerConfiguration _customSanitizerConfiguration;

        public SanitizerConfiguration_Test()
        {
            _defaultSanitizerConfiguration = new SanitizerConfiguration();

            _customMaxInputLength = 10;

            _customReturnOriginalCharacterIfMapNotFound = true;

            _customerReplacementCharacterMap = new Dictionary<string, string> { { "x", "y" } };

            _customSanitizerConfiguration = new SanitizerConfiguration(_customMaxInputLength, _customReturnOriginalCharacterIfMapNotFound, _customerReplacementCharacterMap);
        }

        [Fact]
        public void WhenDefaultConfigurationIsCreated_ThenMaxLengthInputStringShouldBeSetCorrect()
        {
            Assert.Equal(100, _defaultSanitizerConfiguration.MaxLengthInputString);
        }

        [Fact]
        public void WhenDefaultConfigurationIsCreated_ThenReturnOriginalCharacterMapIfNotFoundShouldBeReturnedCorrect()
        {
            Assert.False(_defaultSanitizerConfiguration.ReturnOriginalCharacterIfMapNotFound);
        }

        [Fact]
        public void WhenDefaultConfigurationIsCreated_ThenAnDefaultCharacterMapShouldBeCreated()
        {
            Assert.NotNull(_defaultSanitizerConfiguration.ReplacementCharacterMap);
            Assert.Equal(25, _defaultSanitizerConfiguration.ReplacementCharacterMap.Keys.Count);
        }

        [Fact]
        public void WhenCustomConfigurationIsCreated_ThenMaxLengthInputStringShouldBeSetCorrect()
        {
            Assert.Equal(_customMaxInputLength, _customSanitizerConfiguration.MaxLengthInputString);
        }

        [Fact]
        public void WhenCustomConfigurationIsCreated_ThenReturnOriginalCharacterMapIfNotFoundShouldBeSetCorrect()
        {
            Assert.Equal(_customReturnOriginalCharacterIfMapNotFound, _customSanitizerConfiguration.ReturnOriginalCharacterIfMapNotFound);
        }
        [Fact]
        public void WhenCustomConfigurationIsCreated_ThenCharacterMapShouldBeSetCorrect()
        {
            Assert.NotNull(_customSanitizerConfiguration.ReplacementCharacterMap);
            Assert.Equal(_customerReplacementCharacterMap.Keys.Count, _customSanitizerConfiguration.ReplacementCharacterMap.Keys.Count);
        }
    }
}
