package org.carlspring.strongbox.util;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Collection;

import com.google.common.base.Throwables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.util.Assert;

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
        try
        {
            final URI[] uris = paths.stream().map(Path::toUri).toArray(size -> new URI[size]);
            urls = new URL[uris.length];
            for (int i = 0; i < uris.length; i++)
            {
                urls[i] = uris[i].toURL();
            }
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        return new URLClassLoader(urls, parent);
    }
}
