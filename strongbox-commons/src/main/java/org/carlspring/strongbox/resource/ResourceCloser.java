package org.carlspring.strongbox.resource;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import java.io.Closeable;
import java.io.IOException;

import org.slf4j.Logger;

/**
 * A utility class for safely closing resources and logging eventual errors.
 * The purpose of this class is to avoid boiler-plate code.
 *
 * @author mtodorov
 */
public class ResourceCloser
{

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
                logger.error(e.getMessage(), e);
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
                logger.error(e.getMessage(), e);
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
                logger.error(e.getMessage(), e);
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
                logger.error(e.getMessage(), e);
            }
        }
    }

}
