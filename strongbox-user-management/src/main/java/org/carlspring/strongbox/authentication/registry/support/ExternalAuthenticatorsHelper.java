package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.util.ClassLoaderFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import com.google.common.base.Throwables;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
public class ExternalAuthenticatorsHelper
{

    private static final Pattern AUTHENTICAION_PROVIDER_PATTERN = Pattern.compile(
            "strongbox-.*-authentication-provider.*jar");

    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthenticatorsHelper.class);

    private ExternalAuthenticatorsHelper()
    {

    }

    public static ClassLoader getExternalAuthenticatorsClassLoader(ClassLoader parent)
    {

        File authenticatorsDirectory;
        try
        {
            authenticatorsDirectory = ConfigurationResourceResolver.getConfigurationResource("authentication.lib",
                                                                                             "webapp/WEB-INF/lib").getFile();
        }
        catch (FileNotFoundException e)
        {
            logger.debug(e.getMessage());
            return parent;
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        if (!authenticatorsDirectory.exists())
        {
            logger.debug(authenticatorsDirectory + " does not exist.");
            return parent;
        }
        if (!authenticatorsDirectory.isDirectory())
        {
            logger.error(authenticatorsDirectory + " is not a directory.");
            return parent;
        }

        final File[] authenticatorsJars = authenticatorsDirectory.listFiles(
                ExternalAuthenticatorsHelper::authenticationProviderFilter);
        if (ArrayUtils.isEmpty(authenticatorsJars))
        {
            logger.debug(authenticatorsDirectory + "contains 0 authenticators jar files.");
            return null;
        }

        return ClassLoaderFactory.urlClassLoaderFromFiles(parent, authenticatorsJars);
    }

    private static boolean authenticationProviderFilter(File pathname)
    {
        return !pathname.isDirectory() && AUTHENTICAION_PROVIDER_PATTERN.matcher(pathname.getName()).matches();
    }

}