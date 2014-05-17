package org.carlspring.strongbox.xml.parsers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.StorageMapEntryConverter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.Map;

import com.thoughtworks.xstream.XStream;

/**
 * @author mtodorov
 */
public class ConfigurationParser extends GenericParser<Configuration>
{

    public XStream getXStreamInstance()
    {
        XStream xstream = new XStream();
        xstream.autodetectAnnotations(true);
        xstream.alias("configuration", Configuration.class);
        xstream.alias("storages", Map.class);
        xstream.alias("storage", Storage.class);
        xstream.alias("repositories", Map.class);
        xstream.alias("repository", Repository.class);
        xstream.registerConverter(new StorageMapEntryConverter());

        return xstream;
    }

}
