package org.carlspring.strongbox.yaml;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.util.ServiceLoaderUtils;

import java.io.*;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public abstract class YamlFileManager<T>
{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Class<T> myClazz;

    private final YAMLMapper yamlMapper;

    public YamlFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        this(yamlMapperFactory, new Class[0]);
    }

    @SuppressWarnings("unchecked")
    public YamlFileManager(YAMLMapperFactory yamlMapperFactory,
                           Class<?>... classes)
    {
        myClazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), YamlFileManager.class);
        yamlMapper = yamlMapperFactory.create(ServiceLoaderUtils.load(classes));
    }

    public abstract String getPropertyKey();

    public abstract String getDefaultLocation();

    public abstract ConfigurationResourceResolver getConfigurationResourceResolver();

    public Resource getResource()
    {
        return getConfigurationResourceResolver().getConfigurationResource(getPropertyKey(), getDefaultLocation());
    }

    public synchronized void store(final T configuration)
    {
        try
        {
            Resource resource = getResource();
            //Check that target resource stored on FS and not under classpath for example
            if (!resource.isFile() || resource instanceof ClassPathResource)
            {
                logger.warn(String.format("Skip resource store [%s]", resource));
                return;
            }

            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(Paths.get(resource.getURI()))))
            {
                yamlMapper.writeValue(os, configuration);
            }
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

    public synchronized T read()
    {
        Resource resource = getResource();
        if (!resource.exists())
        {
            return null;
        }

        try (InputStream inputStream = new BufferedInputStream(resource.getInputStream()))
        {
            return yamlMapper.readValue(inputStream, myClazz);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

}
