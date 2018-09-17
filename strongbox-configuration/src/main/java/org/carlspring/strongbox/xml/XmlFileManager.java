package org.carlspring.strongbox.xml;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 */
public abstract class XmlFileManager<T>
{

    private final GenericParser<T> parser;

    public XmlFileManager()
    {
        parser = new GenericParser<>((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public XmlFileManager(Class... classes)
    {
        Set<Class<?>> set = new HashSet<>();
        set.add((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        Stream.of(classes).forEach(
                clazz ->
                {
                    ServiceLoader<?> loader = ServiceLoader.load(clazz);
                    loader.forEach(impl ->
                                   {
                                       set.add(impl.getClass());
                                   });
                }
        );
        parser = new GenericParser<>(set.toArray(new Class[0]));
    }

    public abstract String getPropertyKey();

    public abstract String getDefaultLocation();

    public Resource getResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource(getPropertyKey(), getDefaultLocation());
    }

    public void store(final T configuration)
    {
        try
        {
            parser.store(configuration, getResource());
        }
        catch (JAXBException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public T read()
    {
        try (InputStream inputStream = new BufferedInputStream(getResource().getInputStream()))
        {
            return parser.parse(inputStream);
        }
        catch (JAXBException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}
