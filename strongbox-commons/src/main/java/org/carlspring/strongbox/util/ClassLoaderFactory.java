package org.carlspring.strongbox.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import com.google.common.base.Throwables;
import org.springframework.util.Assert;
import static org.apache.commons.io.FileUtils.toURLs;

/**
 * @author Przemyslaw Fusik
 */
public final class ClassLoaderFactory
{

    private ClassLoaderFactory()
    {

    }

    @SafeVarargs
    public static URLClassLoader urlClassLoaderFromFiles(ClassLoader parent,
                                                         File... files)
    {
        Assert.notNull(files, "files array cannot be null");

        URL[] urls;
        try
        {
            urls = toURLs(files);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }

        return new URLClassLoader(urls, parent);
    }
}
