namespace SDL.ECommerce.Formatting.Servants
{
    using System.Collections.Generic;

    public interface ISanitizerConfiguration
    {
        int MaxLengthInputString { get; }

        bool ReturnOriginalCharacterIfMapNotFound { get; set; }

        Dictionary<string, string> ReplacementCharacterMap { get; }
    }
}
