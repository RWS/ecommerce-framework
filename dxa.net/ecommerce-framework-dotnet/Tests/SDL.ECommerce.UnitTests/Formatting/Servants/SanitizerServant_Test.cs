namespace SDL.ECommerce.UnitTests.Formatting.Servants
{
    using System;
    using ECommerce.Formatting.Servants;
    using Xunit;

    public class SanitizerServantTest
    {
        private readonly SanitizerServant _sanitizerServant;

        public SanitizerServantTest()
        {
            var config = new SanitizerConfiguration();
            _sanitizerServant = new SanitizerServant(config);
        }

        [Fact]
        public void WhenConfigurationMaxLengthStringIsLessThanOne_ThenArgumentExceptionShouldbeThrown()
        {
            var configuration = new SanitizerConfiguration(maxInputLengthString: 0);
            Assert.Throws<ArgumentException>(() => new SanitizerServant(configuration));
        }

        [Theory]
        [InlineData("àåáâäãåąæąāăǎǟǡǣǻǽȁȃȧa", "a")]
        [InlineData("ìíîïıĩīĭįıǐȉȋɩɨi", "i")]
        [InlineData("èéêëęěİēėĕȅȇȩ", "e")]
        [InlineData("òóôõöøőðœōŏǒǫǭǿȍȏȫȭȯȱɵơơ", "o")]
        [InlineData("ùúûüŭůűũūųǔǖǘǚǜȕȗư", "u")]
        [InlineData("çćčĉċƈ", "c")]
        [InlineData("żźžȥƶ", "z")]
        [InlineData("śşšŝș", "s")]
        [InlineData("ñńňņŉŋǹ", "n")]
        [InlineData("ýÿŷȳƴ", "y")]
        [InlineData("ğĝģġǥǧǵ", "g")]
        [InlineData("řŕŗȑȓʀ", "r")]
        [InlineData("łľŀļĺ", "l")]
        [InlineData("đďȡɖɗƌ", "d")]
        [InlineData("ßẞ", "ss")]
        [InlineData("ÞÞþ", "th")]
        [InlineData("ĥħȟ", "h")]
        [InlineData("ĵǰ", "j")]
        [InlineData("ťţŧțƫ", "t")]
        [InlineData("ķĸǩƙ", "k")]
        [InlineData("ŵ", "w")]
        [InlineData("ƿ", "p")]
        [InlineData("ƀɓƃƃƅ", "b")]
        [InlineData("ƒ", "f")]
        [InlineData("'/&.\"®", "")]
        public void WhenNonAsciiCharactersIsReceived_ThenItShouldBeMappedToCorrectAsciiCharacter(string nonAsciiCharacters, string mapsToCharacter)
        {
            Random rnd = new Random(Guid.NewGuid().GetHashCode());
            var randomNumberFromInput = rnd.Next(nonAsciiCharacters.Length);
            Assert.Equal(mapsToCharacter, _sanitizerServant.SanitizedUrlString(nonAsciiCharacters.Substring(randomNumberFromInput, 1)));
        }

        [Theory]
        [InlineData("àåáâäãåąæąāăǎǟǡǣǻǽȁȃȧa")]
        [InlineData("èéêëęěİēėĕȅȇȩ")]
        [InlineData("ìíîïıĩīĭįıǐȉȋɩɨi")]
        [InlineData("òóôõöøőðœōŏǒǫǭǿȍȏȫȭȯȱɵơơ")]
        [InlineData("ùúûüŭůűũūųǔǖǘǚǜȕȗư")]
        [InlineData("çćčĉċƈ")]
        [InlineData("żźžȥƶ")]
        [InlineData("śşšŝș")]
        [InlineData("ñńňņŉŋǹ")]
        [InlineData("ýÿŷȳƴ")]
        [InlineData("ğĝģġǥǧǵ")]
        [InlineData("řŕŗȑȓʀ")]
        [InlineData("łľŀļĺ")]
        [InlineData("đďȡɖɗƌ")]
        [InlineData("ĥħȟ")]
        [InlineData("ĵǰ")]
        [InlineData("ťţŧțƫ")]
        [InlineData("ķĸǩƙ")]
        [InlineData("ŵ")]
        [InlineData("ƿ")]
        [InlineData("ƀɓƃƃƅ")]
        [InlineData("ƒ")]
        public void WhenNonAsciiCharactersIsReceivedThen_ThenAllCharacterShouldBeMappedCorrectInString(string nonAsciiCharacters)
        {
            Assert.Equal(nonAsciiCharacters.Length, _sanitizerServant.SanitizedUrlString(nonAsciiCharacters).Length);
        }

        [Theory]
        [InlineData("ßẞ")]
        [InlineData("ÞÞþ")]
        public void WhenNonAscciCharactersThatMapsToDoubleLettersIsReceived_ThenAllCharactersShouldBeMappedCorrect(string nonAsciiCharacters)
        {
            Assert.Equal(nonAsciiCharacters.Length * 2, _sanitizerServant.SanitizedUrlString(nonAsciiCharacters).Length);
        }

        [Fact]
        public void WhenSendingInputStringWithSpaces_ItShouldBeMappedCorrectWithDashes()
        {
            var result = _sanitizerServant.SanitizedUrlString("därför skall vi testa");
            Assert.Equal("darfor-skall-vi-testa", result);
        }

        [Fact]
        public void WhenCertainSpecialCharactersAreEncountered_ThenEmptyStringShouldBeReturned()
        {
            var result = _sanitizerServant.SanitizedUrlString("'/&.\"®");
            Assert.Equal(string.Empty, result);
        }

        [Fact]
        public void WhenEmptyStringIsSentToSanitizer_ThenEmptyStringShouldBeReturned()
        {
            var result = _sanitizerServant.SanitizedUrlString(string.Empty);
            Assert.Equal(string.Empty, result);
        }

        [Fact]
        public void WhenNullIsSentToSanitizer_ThenEmptyStringShouldBeReturned()
        {
            var result = _sanitizerServant.SanitizedUrlString(null);
            Assert.Equal(string.Empty, result);
        }

    }
}
