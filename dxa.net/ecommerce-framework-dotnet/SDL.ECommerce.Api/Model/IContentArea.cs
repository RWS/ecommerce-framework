namespace SDL.ECommerce.Api.Model
{
    public interface IContentArea
    {
        ILocation Location { get; }
        int X1 { get; }
        int Y1 { get; }
        int X2 { get; }
        int Y2 { get; }
    }
}
