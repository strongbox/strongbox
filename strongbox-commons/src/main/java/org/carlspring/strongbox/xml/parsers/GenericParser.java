package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.url.ClasspathURLStreamHandler;
import org.carlspring.strongbox.url.ClasspathURLStreamHandlerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

/**
 * @author mtodorov
 */
public abstract class GenericParser<T>
{

    static {
        final ClasspathURLStreamHandler handler = new ClasspathURLStreamHandler(ClassLoader.getSystemClassLoader());
        ClasspathURLStreamHandlerFactory factory = new ClasspathURLStreamHandlerFactory("classpath", handler);
        URL.setURLStreamHandlerFactory(factory);
    }

    public T parse(String xmlFile)
            throws IOException
    {
        File file = new File(xmlFile).getAbsoluteFile();
        InputStream is = new FileInputStream(file);

        return parse(is);
    }

    public T parse(File xmlFile)
            throws IOException
    {
        InputStream is = new FileInputStream(xmlFile.getAbsoluteFile());

        return parse(is);
    }

    public T parse(URL url)
            throws IOException
    {
        InputStream is = url.openStream();

        return parse(is);
    }

    public T parse(InputStream is)
            throws IOException
    {
        XStream xstream = getXStreamInstance();

        T object;
        try
        {
            //noinspection unchecked
            object = (T) xstream.fromXML(is);

            is.close();
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }

        return object;
    }

    public void store(T object, String path)
            throws IOException
    {
        internalStore(object, path);
    }

    public void store(T object, File file)
            throws IOException
    {
        internalStore(object, file);
    }

    public void store(List<T> object, String path)
            throws IOException
    {
        internalStore(object, path);
    }

    public void store(List<T> object, File file)
            throws IOException
    {
        internalStore(object, file);
    }

    public void store(T object, OutputStream outputStream)
            throws IOException
    {
        internalStore(object, outputStream);
    }

    public void store(List<T> object, OutputStream outputStream)
            throws IOException
    {
        internalStore(object, outputStream);
    }

    private void store(String xml, OutputStream outputStream)
            throws IOException
    {
        outputStream.write(xml.getBytes());
        outputStream.flush();
    }

    protected void internalStore(Object object, String path)
            throws IOException
    {
        internalStore(object, new File(path).getCanonicalFile());
    }

    protected void internalStore(Object object, File file)
            throws IOException
    {
        XStream xstream = getXStreamInstance();

        char[] indent = new char[] { ' ', ' ', ' ', ' ' };

        FileOutputStream fos = new FileOutputStream(file);
        xstream.marshal(object, new PrettyPrintWriter(new OutputStreamWriter(fos), indent));
    }

    protected void internalStore(Object object, OutputStream outputStream)
            throws IOException
    {
        char[] indent = new char[] { ' ', ' ', ' ', ' ' };

        XStream xstream = getXStreamInstance();
        xstream.marshal(object, new PrettyPrintWriter(new OutputStreamWriter(outputStream), indent));
    }

    public abstract XStream getXStreamInstance();

}
