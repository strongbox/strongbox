package org.carlspring.strongbox.npm.metadata.jackson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.carlspring.strongbox.npm.metadata.Bin;
import org.carlspring.strongbox.npm.metadata.Directories;
import org.carlspring.strongbox.npm.metadata.Engines;
import org.carlspring.strongbox.npm.metadata.License;
import org.carlspring.strongbox.npm.metadata.Person;
import org.carlspring.strongbox.npm.metadata.Repository;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class NpmJacksonMapperFactory
{

    public static ObjectMapper createObjectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                                                      .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        objectMapper.setDateFormat(df);

        SimpleModule module = new SimpleModule();
        
        module.addDeserializer(Repository.class, new RepositoryDeserializer());
        module.addDeserializer(License.class, new LicenseDeserializer());
        module.addDeserializer(Bin.class, new BinDeserializer());
        module.addDeserializer(Engines.class, new EnginesDeserializer());
        module.addDeserializer(Person.class, new PersonDeserializer());
        module.addDeserializer(Directories.class, new DirectoriesDeserializer());
        
        objectMapper.registerModule(module);

        return objectMapper;
    }

}
