package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.url.ClasspathURLStreamHandler;
import org.carlspring.strongbox.url.ClasspathURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mtodorov
 */
public class GenericParser<T>
{

    private static final Logger logger = LoggerFactory.getLogger(GenericParser.class);

    private ReentrantLock lock = new ReentrantLock();

    private Set<Class> classes = new LinkedHashSet<Class>();

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

    public GenericParser(Class... classes)
    {
        Collections.addAll(this.classes, classes);
    }

    public T parse(File file)
            throws JAXBException, FileNotFoundException
    {
        FileInputStream is = null;
        T object = null;

        try
        {
            is = new FileInputStream(file);
            object = parse(is);
        }
        finally
        {
            ResourceCloser.close(is, logger);
        }

        return object;
    }

    public T parse(URL url)
            throws IOException, JAXBException
    {
        InputStream is = null;

        try
        {
            is = url.openStream();

            return parse(is);
        }
        finally
        {
            ResourceCloser.close(is, logger);
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

    public void store(T object, String path)
            throws JAXBException, FileNotFoundException
    {
        store(object, new File(path).getAbsoluteFile());
    }

    public void store(T object, File file)
            throws JAXBException, FileNotFoundException
    {
        FileOutputStream os = null;

        try
        {
            os = new FileOutputStream(file);
            store(object, os);
        }
        finally
        {
            ResourceCloser.close(os, logger);
        }
    }

    public void store(T object, OutputStream os)
            throws JAXBException
    {
        try
        {
            lock.lock();

            JAXBContext context = getContext();

            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshaller.marshal(object, os);
        }
        finally
        {
            lock.unlock();
        }
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
