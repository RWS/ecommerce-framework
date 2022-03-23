namespace SDL.ECommerce.Formatting.Servants
{
    using System.Collections.Generic;

    public class SanitizerConfiguration : ISanitizerConfiguration
    {
        public SanitizerConfiguration(int maxInputLengthString = 100, bool returnOriginalCharacterIfMapNotFound = false, Dictionary<string, string> replacementCharacterMap = null)
        {
            MaxLengthInputString = maxInputLengthString;

            ReturnOriginalCharacterIfMapNotFound = returnOriginalCharacterIfMapNotFound;

            ReplacementCharacterMap = replacementCharacterMap ?? CreateReplacementCharMap();
        }

        public int MaxLengthInputString { get; set; }

        public Dictionary<string, string> ReplacementCharacterMap { get; set; }

        public bool ReturnOriginalCharacterIfMapNotFound { get; set; }

        private Dictionary<string, string> CreateReplacementCharMap()
        {
            return new Dictionary<string, string>()
            {
                {"a", "àåáâäãåąæąāăǎǟǡǣǻǽȁȃȧa"},
                {"e", "èéêëęěİēėĕȅȇȩ"},
                {"i", "ìíîïıĩīĭįıǐȉȋɩɨi"},
                {"o", "òóôõöøőðœōŏǒǫǭǿȍȏȫȭȯȱɵơơ"},
                {"u", "ùúûüŭůűũūųǔǖǘǚǜȕȗư"},
                {"c", "çćčĉċƈ"},
                {"z", "żźžȥƶ"},
                {"s", "śşšŝș"},
                {"n", "ñńňņŉŋǹ"},
                {"y", "ýÿŷȳƴ"},
                {"g", "ğĝģġǥǧǵ"},
                {"r", "řŕŗȑȓʀ"},
                {"l", "łľŀļĺ"},
                {"d", "đďȡɖɗƌ"},
                {"ss", "ßẞ"},
                {"th", "ÞÞþ"},
                {"h", "ĥħȟ"},
                {"j", "ĵǰ"},
                {"t", "ťţŧțƫ"},
                {"k", "ķĸǩƙ"},
                {"w", "ŵ"},
                {"p", "ƿ"},
                {"b", "ƀɓƃƃƅ"},
                {"f", "ƒ"},
                { string.Empty, "'/&.\"®"}
            };
        }
    }
}