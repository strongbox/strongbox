package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.url.ClasspathURLStreamHandler;
import org.carlspring.strongbox.url.ClasspathURLStreamHandlerFactory;
import org.carlspring.strongbox.xml.CustomTagService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class GenericParser<T>
{

    public final static boolean IS_OUTPUT_FORMATTED = true;

    private static final Logger logger = LoggerFactory.getLogger(GenericParser.class);

    private ReentrantLock lock = new ReentrantLock();

    private Set<Class> classes = new LinkedHashSet<>();

    private JAXBContext context;


    static
    {
        final ClasspathURLStreamHandler handler = new ClasspathURLStreamHandler(ClassLoader.getSystemClassLoader());
        ClasspathURLStreamHandlerFactory factory = new ClasspathURLStreamHandlerFactory("classpath", handler);
        try
        {
            URL.setURLStreamHandlerFactory(factory);
        }
        catch (Error e)
        {
            // You can safely disregard this, as a second attempt to register a an already
            // registered URLStreamHandlerFactory will throw an error. Since there's no
            // apparent way to check if it's registered, just catch and ignore the error.
        }
    }

    public GenericParser()
    {
        this.classes.addAll(CustomTagService.getInstance().getImplementations());
    }

    public GenericParser(Class... classes)
    {
        Collections.addAll(this.classes, classes);
        this.classes.addAll(CustomTagService.getInstance().getImplementations());
    }

    public T parse(File file)
            throws JAXBException, IOException
    {
        T object = null;

        try (FileInputStream is = new FileInputStream(file))
        {
            object = parse(is);
        }

        return object;
    }

    public T parse(URL url)
            throws IOException, JAXBException
    {

        try (InputStream is = url.openStream())
        {
            return parse(is);
        }
    }

    public T parse(InputStream is)
            throws JAXBException
    {
        T object = null;

        try
        {
            lock.lock();

            Unmarshaller unmarshaller = getContext().createUnmarshaller();

            //noinspection unchecked
            object = (T) unmarshaller.unmarshal(is);
        }
        finally
        {
            lock.unlock();
        }

        return object;
    }

    public void store(T object,
                      String path)
            throws JAXBException, IOException
    {
        store(object, new File(path).getAbsoluteFile());
    }

    public void store(T object,
                      File file)
            throws JAXBException, IOException
    {

        try (FileOutputStream os = new FileOutputStream(file))
        {
            store(object, os);
        }
    }

    public void store(T object,
                      OutputStream os)
            throws JAXBException
    {
        try
        {
            lock.lock();

            JAXBContext context = getContext();

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, IS_OUTPUT_FORMATTED);

            marshaller.marshal(object, os);
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Serialize #object to String using JAXB marshaller.
     *
     * @param object the object to be serialized
     * @return String representation of object
     */
    public String serialize(T object)
            throws JAXBException
    {
        StringWriter writer = new StringWriter();
        try
        {
            lock.lock();

            JAXBContext context = getContext();

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, IS_OUTPUT_FORMATTED);

            marshaller.marshal(object, writer);
            return writer.getBuffer().toString();
        }
        finally
        {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public T deserialize(String input)
            throws JAXBException
    {
        try
        {
            lock.lock();

            JAXBContext context = getContext();
            Unmarshaller m = context.createUnmarshaller();
            return (T) m.unmarshal(new StringReader(input));
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setContext(Class<?> classType)
            throws JAXBException
    {
        context = JAXBContext.newInstance(classType);
    }

    public JAXBContext getContext()
            throws JAXBException
    {
        if (context == null)
        {
            context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
        }

        return context;
    }

}
