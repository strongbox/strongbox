package org.carlspring.strongbox.xml.parsers;

import com.thoughtworks.xstream.XStream;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.StorageMapEntryConverter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.List;
import java.util.Map;

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
