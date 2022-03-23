namespace SDL.ECommerce.DXA.Providers
{
    using System;
    using System.Threading;
    using System.Web.Mvc;

    public static class DependencyResolverProvider
    {
        private static readonly ThreadLocal<Func<Func<Type, object>>> GetDependencies = new ThreadLocal<Func<Func<Type, object>>>(() => () => DependencyResolver.Current.GetService);

        internal static Func<Type, object> Dependencies => GetDependencies.Value();

        internal static void Set(Func<Type, object> dependencies)
        {
            GetDependencies.Value = () => dependencies;
        }

        internal static void Reset()
        {
            GetDependencies.Value = () => DependencyResolver.Current.GetService;
        }
    }
}