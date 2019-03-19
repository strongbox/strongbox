package org.carlspring.strongbox.yaml;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public abstract class YamlFileManager<T>
{

    private Class<T> myClazz;

    private final YAMLMapper yamlMapper;

    public YamlFileManager()
    {
        this(new Class[0]);
    }

    @SuppressWarnings("unchecked")
    public YamlFileManager(Class<?>... classes)
    {
        myClazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), YamlFileManager.class);

        Set<Class<?>> contextClasses = new HashSet<>();
        if (classes != null)
        {
            Arrays.asList(classes).forEach(
                    clazz ->
                    {
                        ServiceLoader<?> loader = ServiceLoader.load(clazz);
                        loader.forEach(impl -> contextClasses.add(impl.getClass()));
                    }
            );
        }

        yamlMapper = new CustomYamlMapper(contextClasses);
    }

    public abstract String getPropertyKey();

    public abstract String getDefaultLocation();

    public abstract ConfigurationResourceResolver getConfigurationResourceResolver();

    public Resource getResource()
    {
        return getConfigurationResourceResolver().getConfigurationResource(getPropertyKey(), getDefaultLocation());
    }

    // TODO this needs to be synchronized here or below
    public void store(final T configuration)
    {
        try
        {
            File inputFile = getResource().getFile();
            yamlMapper.writeValue(inputFile, configuration);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

    // TODO this needs to be synchronized here or below
    public T read()
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
