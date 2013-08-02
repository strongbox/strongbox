package org.carlspring.repositoryunit.configuration;

import com.thoughtworks.xstream.XStream;
import org.carlspring.repositoryunit.storage.Storage;
import org.carlspring.repositoryunit.storage.repository.Repository;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author mtodorov
 */
public class ConfigurationParser
{

    public Configuration parseConfiguration(String xmlFile)
            throws FileNotFoundException
    {
        File file = new File(xmlFile).getAbsoluteFile();
        FileInputStream fis = new FileInputStream(file);

        XStream xstream = getXStreamInstance();

        return (Configuration) xstream.fromXML(fis);
    }

    public void storeConfiguration(Configuration configuration, String file)
            throws IOException
    {
        XStream xstream = getXStreamInstance();
        final String xml = xstream.toXML(configuration);

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            fos.write(xml.getBytes());
            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }

    private XStream getXStreamInstance()
    {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.alias("configuration", Configuration.class);
        xstream.alias("resolvers", List.class);
        xstream.alias("resolver", String.class);
        xstream.alias("storages", Map.class);
        xstream.alias("storage", Storage.class);
        xstream.alias("repositories", Map.class);
        xstream.alias("repository", Repository.class);
        xstream.registerConverter(new StorageMapEntryConverter());

        return xstream;
    }

}
