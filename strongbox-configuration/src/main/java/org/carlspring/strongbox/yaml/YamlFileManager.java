package org.carlspring.strongbox.yaml;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public abstract class YamlFileManager<T>
{

    private Class myClazz;
    private ObjectMapper yamlMapper;


    public YamlFileManager()
    {
        this(new Class[0]);
    }

    @SuppressWarnings("unchecked")
    public YamlFileManager(Class... classes)
    {
        myClazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Stream.of(classes).forEach(ServiceLoader::load);

        yamlMapper = new YAMLMapper().configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
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
        Resource resource;
        resource = getResource();
        if (!resource.exists())
        {
            return null;
        }

        try (InputStream inputStream = new BufferedInputStream(resource.getInputStream()))
        {
            JavaType type = yamlMapper.getTypeFactory().constructType(myClazz);
            return yamlMapper.readValue(inputStream, type);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
    }

}
