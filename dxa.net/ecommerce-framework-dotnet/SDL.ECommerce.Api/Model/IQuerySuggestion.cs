namespace SDL.ECommerce.Api.Model
{
    public interface IQuerySuggestion
    {
        string Original { get; }
        string Suggestion { get; }
        int? EstimatedResults { get; }
    }
}
