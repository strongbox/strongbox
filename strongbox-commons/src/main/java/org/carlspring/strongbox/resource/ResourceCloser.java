package org.carlspring.strongbox.resource;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;

import org.slf4j.Logger;

/**
 * A utility class for safely closing resources and logging eventual errors.
 * The purpose of this class is to avoid boiler-plate code.
 *
 * @author mtodorov
 */
public class ResourceCloser
{

    private ResourceCloser() {
    }

    public static void close(Closeable resource, Logger logger)
    {
        if (resource != null)
        {
            try
            {
                resource.close();
            }
            catch (IOException e)
            {
                if (logger != null)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static void close(AutoCloseable resource, Logger logger)
    {
        if (resource != null)
        {
            try
            {
                resource.close();
            }
            catch (Exception e)
            {
                if (logger != null)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static void close(Context resource, Logger logger)
    {
        if (resource != null)
        {
            try
            {
                resource.close();
            }
            catch (Exception e)
            {
                if (logger != null)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static void close(NamingEnumeration resource, Logger logger)
    {
        if (resource != null)
        {
            try
            {
                resource.close();
            }
            catch (Exception e)
            {
                if (logger != null)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static void closeWithReflection(Object resource, Logger logger)
    {
        if (resource != null)
        {
            try
            {
                try
                {
                    Method m = resource.getClass().getMethod("close");
                    m.invoke(resource);
                }
                catch (NoSuchMethodException e)
                {
                    logger.warn("Failed to close resource, as " + resource.getClass() + " does not implement a close() method.");
                }
            }
            catch (Exception e)
            {
                if (logger != null)
                {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

}
