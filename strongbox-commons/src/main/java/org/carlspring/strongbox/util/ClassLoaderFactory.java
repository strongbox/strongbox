package org.carlspring.strongbox.util;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Throwables;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
public final class ClassLoaderFactory
{

    private ClassLoaderFactory()
    {

    }

    public static URLClassLoader urlClassLoaderFromPaths(ClassLoader parent,
                                                         Collection<Path> paths)
    {
        Assert.notNull(paths, "paths array cannot be null");

        final Set<URL> urls = new HashSet<>();
        try
        {
            final Set<URI> uris = paths.stream().map(Path::toUri).collect(Collectors.toSet());
            for (URI uri : uris)
            {
                urls.add(uri.toURL());
            }
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }

        return new URLClassLoader(urls.toArray(new URL[0]), parent);
    }
}
