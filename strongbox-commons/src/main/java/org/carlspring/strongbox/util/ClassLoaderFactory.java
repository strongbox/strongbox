package org.carlspring.strongbox.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Przemyslaw Fusik
 */
public final class ClassLoaderFactory
{

    private ClassLoaderFactory()
    {

    }

    @SuppressFBWarnings(value = "DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED")
    public static URLClassLoader urlClassLoaderFromPaths(ClassLoader parent,
                                                         Collection<Path> paths)
    {
        Assert.notNull(paths, "paths collection cannot be null");

        final URL[] urls;
        final URI[] uris = paths.stream().map(Path::toUri).toArray(URI[]::new);
        urls = new URL[uris.length];
        for (int i = 0; i < uris.length; i++)
        {
            urls[i] = ThrowingFunction.unchecked(URI::toURL).apply(uris[i]);
        }

        return new URLClassLoader(urls, parent);
    }
}
