namespace SDL.ECommerce.DXA.Models
{
    /// <summary>
    /// Lister Navigation Data
    /// </summary>
    public class ListerNavigationData
    {
        public bool ShowNavigation { get; set; }
        public int ViewSets { get; set; }
        public int CurrentSet { get; set; }
        public string NextUrl { get; set; }
        public string PreviousUrl { get; set; }
        public string FirstUrl { get; set; }
        public string LastUrl { get; set; }
        public int TotalCount { get; set; }
    }
}