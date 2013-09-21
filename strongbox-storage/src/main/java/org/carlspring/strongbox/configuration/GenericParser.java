package org.carlspring.strongbox.configuration;

import java.io.*;
import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * @author mtodorov
 */
public abstract class GenericParser<T>
{

    public T parse(String xmlFile)
            throws IOException
    {
        File file = new File(xmlFile).getAbsoluteFile();
        InputStream is = new FileInputStream(file);

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

    public void store(T object, String file)
            throws IOException
    {
        internalStore(object, file);
    }

    public void store(List<T> object, String file)
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

    protected void internalStore(Object object, String file)
            throws IOException
    {
        XStream xstream = getXStreamInstance();
        final String xml = xstream.toXML(object);

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            store(xml, fos);
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }

    protected void internalStore(Object object, OutputStream outputStream)
            throws IOException
    {
        XStream xstream = getXStreamInstance();
        final String xml = xstream.toXML(object);

        store(xml, outputStream);
    }

    public abstract XStream getXStreamInstance();

}
