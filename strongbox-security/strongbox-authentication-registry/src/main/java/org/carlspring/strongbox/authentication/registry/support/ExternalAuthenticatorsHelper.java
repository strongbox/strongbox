package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.util.ClassLoaderFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Throwables;
import org.apache.commons.collections.CollectionUtils;
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
        Path authenticatorsDirectory;
        try
        {
            authenticatorsDirectory = Paths.get(
                    ConfigurationResourceResolver.getConfigurationResource("strongbox.authentication.lib",
                                                                           "webapp/WEB-INF/lib").getURI());
        }
        catch (FileNotFoundException e)
        {
            logger.debug(e.getMessage());
            return parent;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        if (!Files.exists(authenticatorsDirectory))
        {
            logger.debug(authenticatorsDirectory + " does not exist.");
            return parent;
        }
        if (!Files.isDirectory(authenticatorsDirectory))
        {
            logger.error(authenticatorsDirectory + " is not a directory.");
            return parent;
        }

        final Set<Path> authenticatorsJarPaths = getAuthenticatorJarPaths(authenticatorsDirectory);

        if (CollectionUtils.isEmpty(authenticatorsJarPaths))
        {
            logger.debug(authenticatorsDirectory + "contains 0 authenticators jar files.");
            return parent;
        }

        return ClassLoaderFactory.urlClassLoaderFromPaths(parent, authenticatorsJarPaths);
    }

    private static Set<Path> getAuthenticatorJarPaths(final Path authenticatorsDirectory)
    {
        final Set<Path> authenticatorsJarPaths = new HashSet<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(authenticatorsDirectory))
        {
            for (Path path : directoryStream)
            {
                if (isAuthenticatorJarPath(path))
                {
                    authenticatorsJarPaths.add(path);
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return authenticatorsJarPaths;
    }

    private static boolean isAuthenticatorJarPath(Path path)
    {
        return path != null && !Files.isDirectory(path) &&
               AUTHENTICAION_PROVIDER_PATTERN.matcher(path.getFileName().toString()).matches();
    }

}
