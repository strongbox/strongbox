package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.util.JarFileClassLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

import com.google.common.base.Throwables;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsClassLoader
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorsClassLoader.class);

    private AuthenticatorsClassLoader()
    {

    }

    public static void loadAuthenticatorsClasses()
    {

        File authenticatorsDirectory = null;
        try
        {
            authenticatorsDirectory = ConfigurationResourceResolver.getConfigurationResource("authentication.lib",
                                                                                             "webapp/WEB-INF/lib").getFile();
        }
        catch (FileNotFoundException e)
        {
            logger.debug(e.getMessage(), e);
            return;
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        if (!authenticatorsDirectory.exists())
        {
            logger.debug(authenticatorsDirectory + " does not exist.");
            return;
        }
        if (!authenticatorsDirectory.isDirectory())
        {
            logger.error(authenticatorsDirectory + " is not a directory.");
            return;
        }

        final File[] authenticatorsJars = authenticatorsDirectory.listFiles(name -> name.getName().endsWith(".jar"));
        if (ArrayUtils.isEmpty(authenticatorsJars))
        {
            logger.debug(authenticatorsDirectory + "contains 0 authenticators jar files.");
            return;
        }

        Stream.of(authenticatorsJars).forEach(jarFile ->
                                              {
                                                  try
                                                  {
                                                      JarFileClassLoader.loadClasses(
                                                              Thread.currentThread().getContextClassLoader(),
                                                              jarFile.getAbsolutePath());
                                                  }
                                                  catch (IOException | ClassNotFoundException e)
                                                  {
                                                      throw Throwables.propagate(e);
                                                  }
                                              });

    }

}