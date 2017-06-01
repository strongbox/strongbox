package org.carlspring.strongbox.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.google.common.base.Throwables;

/**
 * @author Przemyslaw Fusik
 */
public final class ClassLoaderUtils
{

    private ClassLoaderUtils()
    {

    }

    public static URLClassLoader getURLClassLoader(ClassLoader classLoader)
    {
        if (classLoader == null)
        {
            return null;
        }
        if (classLoader instanceof URLClassLoader)
        {
            return (URLClassLoader) classLoader;
        }
        return getURLClassLoader(classLoader.getParent());
    }

    public static void addURLToURLClassLoader(URLClassLoader cl,
                                              URL[] urls)
    {
        try
        {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{ URL.class });
            method.setAccessible(true);
            method.invoke(cl, urls);
        }
        catch (Exception t)
        {
            Throwables.propagate(t);
        }
    }

    public static void addJarForClassLoading(ClassLoader classLoader,
                                             String pathToJar)
            throws IOException
    {

        final URLClassLoader cl = getURLClassLoader(classLoader);
        addURLToURLClassLoader(cl, new URL[]{ new URL("jar:file:" + pathToJar + "!/") });
    }
}
