namespace SDL.ECommerce.Api.Model
{
    /// <summary>
    /// Category Reference interface
    /// </summary>
    public interface ICategoryRef
    {

        string Id { get; }

        string Name { get; }
      
        string Path { get; }
        
    }

}