using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace SDL.ECommerce.Api
{
   /// <summary>
   ///  Facet Parameter.
   /// Is used for representing a user facing facet.
   /// </summary>
    public class FacetParameter {

        // TODO: Optimize the implementation to minimize the parsing of the facet _values
        // TODO: Combine this together with the Facet. This seems rather being an implementation of the facet for web implementations

        /// <summary>
        /// Parameter Type
        /// </summary>
        public enum ParameterType
        {
            SINGLEVALUE,
            MULTISELECT,
            RANGE,
            LESS_THAN,
            GREATER_THAN
        }

        private static Regex RANGE_PATTERN = new Regex(@"(?'min'[0-9]+[\.0-9]*)-(?'max'[0-9]+[\.0-9]*)", RegexOptions.Compiled);
        private static Regex LESS_THAN_PATTERN = new Regex(@"<(?'lt'[0-9]+[\\.0-9]*)", RegexOptions.Compiled);
        private static Regex GREATER_THAN_PATTERN = new Regex(@">(?'gt'[0-9]+[\\.0-9]*)", RegexOptions.Compiled);

        private string _name;
        private List<string> _values = new List<string>();
        private ParameterType type;

        /// <summary>
        /// Create new facet parameter
        /// </summary>
        /// <param name="name"></param>
        public FacetParameter(string name)
        {
            _name = name;
        }

        /// <summary>
        ///  Create a new facet. The value is parsed to extract current type and _values.
        /// </summary>
        /// <param name="name"></param>
        /// <param name="strValue"></param>
        public FacetParameter(string name, string strValue)
        {
            _name = name;

            var rangeMatcher = RANGE_PATTERN.Match(strValue);
            var lessThanMatcher = LESS_THAN_PATTERN.Match(strValue);
            var greaterThanMatcher = GREATER_THAN_PATTERN.Match(strValue);

            if ( name.EndsWith("_val") )
            {
                this._name = this._name.Replace("_val", "");
                this.type = ParameterType.SINGLEVALUE;
                this._values.Add(strValue);
            }
            else if ( rangeMatcher.Success )
            {
                this.type = ParameterType.RANGE;
                String min = rangeMatcher.Groups["min"].Value;
                String max = rangeMatcher.Groups["max"].Value;
                this._values.Add(min);
                this._values.Add(max);
            }
            else if ( lessThanMatcher.Success )
            {
                this.type = ParameterType.LESS_THAN;
                String value = lessThanMatcher.Groups["lt"].Value;
                this._values.Add(value);
            }
            else if (greaterThanMatcher.Success )
            {
                this.type = ParameterType.GREATER_THAN;
                String value = greaterThanMatcher.Groups["gt"].Value;
                this._values.Add(value);
            }
            else
            {
                // Multi-select
                //
                this.type = ParameterType.MULTISELECT;
                var tokens = strValue.Split(new char[] { '|' }, StringSplitOptions.RemoveEmptyEntries);
                foreach ( var token in tokens )
                {
                    _values.Add(token);
                }
            }

        }

        /// <summary>
        /// Name of the facet parameter. Is shown in the facet URL.
        /// </summary>
        /// <returns></returns>
        public string Name
        {
            get
            {
                return _name;
            }
           
        }

        /// <summary>
        /// Get all facet _values.
        /// </summary>
        /// <returns></returns>
        public List<String> Values
        {
            get
            {
                return _values;
            }
        }

        /// <summary>
        ///  Check if specific value is included in this facet parameter (when having multi select facets).
        /// </summary>
        /// <param name="facetValue"></param>
        /// <returns></returns>
        public bool ContainsValue(string facetValue)
        {           
            if ( this.type == ParameterType.RANGE ) {
            
                var rangeMatcher = RANGE_PATTERN.Match(facetValue);
                if (rangeMatcher.Success && this._values.Count == 2) {
                    String min = rangeMatcher.Value;
                    rangeMatcher = rangeMatcher.NextMatch();
                    String max = rangeMatcher.Value;
                    return (this._values[0].Equals(min) && this._values[1].Equals(max));
                }
                return false;
            }
            else {
                return this._values.Contains(facetValue);
            }
        }

        /// <summary>
        /// Parameter Type
        /// </summary>
        /// <returns></returns>
        public ParameterType Type
        {
            get
            {
                return type;
            }
        }

        public bool IsHidden
        {
            get
            {
                return this._name.EndsWith("_hidden");
            }
        }

        public static FacetParameter FromUrl(String facetUrl)
        {
            var urlBits = facetUrl.Split('=');
            if(urlBits.Length < 2){
                return null;
            }

            string name = urlBits[0];
            string strValue = urlBits[1];

            return new FacetParameter(name, strValue);
        }

        /// <summary>
        ///  Convert the facet parameter to URL format.
        /// </summary>
        /// <returns></returns>
        public string ToUrl()
        {
            return AddValueToUrl(null);
        }

        /// <summary>
        /// Convert the facet parameter to URL format.
        /// </summary>
        /// <param name="additionalValue"></param>
        /// <returns></returns>
        public string AddValueToUrl(string additionalValue)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(this._name);
            if ( type == ParameterType.SINGLEVALUE ) {
                sb.Append("_val");
            }
            sb.Append("=");
            if ( type == ParameterType.LESS_THAN ) {
                sb.Append("<");
            }
            else if ( type == ParameterType.GREATER_THAN ) {
                sb.Append(">");
            }
            String delimiter = "";
            if (type == ParameterType.MULTISELECT) {
                delimiter = "|";
            } else if (type == ParameterType.RANGE) {
                delimiter = "-";
            }

            for ( int i=0; i < this._values.Count; i++ ) {
                sb.Append(_values[i]);
                if ( i < _values.Count()-1 ) {
                    sb.Append(delimiter);
                }
            }
            if ( additionalValue != null && type == ParameterType.MULTISELECT ) {
                sb.Append(delimiter);
                sb.Append(additionalValue);
            }
            return sb.ToString();
        }

        /// <summary>
        /// Remove a value from a facet URL fragment (used for breadcrumbs and de-selecting multi-value facets).
        /// </summary>
        /// <param name="removedValue"></param>
        /// <returns></returns>
        public String RemoveValueFromUrl(String removedValue)
        {
            if ( ( _values.Count == 1 && _values[0].Equals(removedValue) ) ||
                    ( type == ParameterType.RANGE && this._values.Count == 2 ) ) {
                return "";
            }
            var sb = new StringBuilder();
            sb.Append(this._name);
            if ( type == ParameterType.SINGLEVALUE ) {
                sb.Append("_val");
            }
            sb.Append("=");
            if ( type == ParameterType.LESS_THAN ) {
                sb.Append("<");
            }
            else if ( type == ParameterType.GREATER_THAN ) {
                sb.Append(">");
            }
            String delimiter = "";
            if (type == ParameterType.MULTISELECT) {
                delimiter = "|";
            } else if (type == ParameterType.RANGE) {
                delimiter = "-";
            }
            List<string> values = new List<string>();
            
            foreach ( var value in this._values ) {
                if ( type != ParameterType.MULTISELECT ||
                        (type == ParameterType.MULTISELECT && !value.Equals(removedValue)) ) {
                    values.Add(value);
                }
            }
            
            for ( int i=0; i < values.Count; i++ ) {
                string value = values[i];          
                sb.Append(value);
                if (i < values.Count - 1)
                {
                    sb.Append(delimiter);
                }              
            }
            return sb.ToString();
        }

    }
}
