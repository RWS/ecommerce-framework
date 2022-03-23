namespace SDL.ECommerce.Formatting.Servants
{
    using System;
    using System.Collections.Generic;
    using System.Text;

    public class SanitizerServant : ISanitizerServant
    {
        private readonly Dictionary<string, string> _replacementCharacterMap;
        private readonly int _maximumInputStringLength;
        private readonly bool _returnOriginalIfNotFound;

        public SanitizerServant(ISanitizerConfiguration configuration)
        {
            if (configuration.MaxLengthInputString <= 0)
            {
                throw new ArgumentException("Maximum Input Length string cannot be less than 1");
            }
            _maximumInputStringLength = configuration.MaxLengthInputString;

            if (configuration.ReplacementCharacterMap == null)
            {
                throw new ArgumentException("Replacement character map cannot be null");
            }
            _replacementCharacterMap = configuration.ReplacementCharacterMap;

            _returnOriginalIfNotFound = configuration.ReturnOriginalCharacterIfMapNotFound;
        }

        public string SanitizedUrlString(string title)
        {
            if (title == null) return string.Empty;

            var maxlen = _maximumInputStringLength > 0 ? _maximumInputStringLength : 80;
            var len = title.Length;
            var prevdash = false;
            var sb = new StringBuilder(len);

            for (var i = 0; i < len; i++)
            {
                var c = title[i];
                if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))
                {
                    sb.Append(c);
                    prevdash = false;
                }
                else if (c >= 'A' && c <= 'Z')
                {
                    // tricky way to convert to lowercase
                    sb.Append((char)(c | 32));
                    prevdash = false;
                }
                else if (c == ' ' || c == ',' || c == '.' || c == '/' ||
                    c == '\\' || c == '-' || c == '_' || c == '=')
                {
                    if (!prevdash && sb.Length > 0)
                    {
                        sb.Append('-');
                        prevdash = true;
                    }
                }
                else if (c >= 128)
                {
                    var prevlen = sb.Length;
                    sb.Append(RemapInternationalCharToAscii(c));
                    if (prevlen != sb.Length) prevdash = false;
                }
                if (i == maxlen) break;
            }

            return prevdash ? sb.ToString().Substring(0, sb.Length - 1) : sb.ToString();
        }

        private string RemapInternationalCharToAscii(char c)
        {
            var s = c.ToString().ToLowerInvariant();
            var result = _returnOriginalIfNotFound ? s : string.Empty;
            foreach (var key in _replacementCharacterMap.Keys)
            {
                if (!_replacementCharacterMap[key].Contains(s))
                {
                    continue;
                }

                result = _returnOriginalIfNotFound ? string.Empty : key;
                break;
            }

            return result;
        }
    }
}