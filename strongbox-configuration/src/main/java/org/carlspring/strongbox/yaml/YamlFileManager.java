package org.carlspring.strongbox.yaml;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
    private ObjectMapper yamlMapper;

    public YamlFileManager()
    {
        this(new Class[0]);
    }

    @SuppressWarnings("unchecked")
    public YamlFileManager(Class... classes)
    {
        myClazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), YamlFileManager.class);

        Stream.of(classes).forEach(ServiceLoader::load);
        Set<Class<?>> contextClasses = new HashSet<>();
        contextClasses.add(myClazz);
        new GenericParser<>(contextClasses.toArray(new Class[0]));

        yamlMapper = new YAMLMapper().enable(SerializationFeature.WRAP_ROOT_VALUE)
                                     .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                                     .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public abstract String getPropertyKey();

    public abstract String getDefaultLocation();

    public abstract ConfigurationResourceResolver getConfigurationResourceResolver();

    public Resource getResource()
    {
        return getConfigurationResourceResolver().getConfigurationResource(getPropertyKey(), getDefaultLocation());
    }

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
