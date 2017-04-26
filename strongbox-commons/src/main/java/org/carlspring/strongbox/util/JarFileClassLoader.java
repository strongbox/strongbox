package org.carlspring.strongbox.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Przemyslaw Fusik
 */
public class JarFileClassLoader
{

    public static void loadClasses(ClassLoader classLoader,
                                   String pathToJar)
            throws IOException, ClassNotFoundException
    {

        final URLClassLoader cl = ClassLoaderUtils.getURLClassLoader(classLoader);
        ClassLoaderUtils.addURLToURLClassLoader(cl, new URL[]{ new URL("jar:file:" + pathToJar + "!/") });

        final JarFile jarFile = new JarFile(pathToJar);
        final Enumeration<JarEntry> e = jarFile.entries();
        while (e.hasMoreElements())
        {
            final JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class"))
            {
                continue;
            }

            String className = je.getName().substring(0, je.getName().length() - 6); // .class
            className = className.replace('/', '.');

            cl.loadClass(className);
        }
    }

}
