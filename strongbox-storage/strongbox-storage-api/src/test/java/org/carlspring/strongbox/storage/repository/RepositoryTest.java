package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.aws.AwsConfiguration;
import org.carlspring.strongbox.storage.repository.gcs.GoogleCloudConfiguration;
import org.carlspring.strongbox.xml.CustomTagService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
public class RepositoryTest
{


    @Test
    public void testAddRepositoryWithCustomConfiguration()
            throws JAXBException
    {
        Repository repository = createTestRepositoryWithCustomConfig();

        Storage storage = new Storage("storage0");
        storage.addOrUpdateRepository(repository);

        Configuration configuration = new Configuration();
        configuration.addStorage(storage);

        GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

        String serialized = parser.serialize(configuration);

        System.out.println(serialized);

        assertTrue(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertTrue(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
    }

    @Test
    public void testMarshallAndUnmarshallSimpleConfiguration()
            throws JAXBException
    {
        Storage storage = new Storage("storage0");

        Repository repository = new Repository("test-repository");
        repository.setStorage(storage);

        storage.addOrUpdateRepository(repository);

        Configuration configuration = new Configuration();
        configuration.addStorage(storage);

        GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

        String serialized = parser.serialize(configuration);

        System.out.println(serialized);

        assertFalse(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertFalse(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));

        Configuration unmarshalledConfiguration = parser.parse(new ByteArrayInputStream(serialized.getBytes()));

        assertNotNull(unmarshalledConfiguration.getStorage("storage0"));
    }

    private Repository createTestRepositoryWithCustomConfig()
    {
        Storage storage = new Storage("storage0");
        Repository repository = new Repository("test-repository");
        repository.setStorage(storage);

        AwsConfiguration awsConfiguration = new AwsConfiguration();
        awsConfiguration.setBucket("test-bucket");
        awsConfiguration.setKey("test-key");

        GoogleCloudConfiguration googleCloudConfiguration = new GoogleCloudConfiguration();
        googleCloudConfiguration.setBucket("test-bucket");
        googleCloudConfiguration.setKey("test-key");

        List<CustomConfiguration> customConfigurations = new ArrayList<>();
        customConfigurations.add(awsConfiguration);
        customConfigurations.add(googleCloudConfiguration);

        repository.setCustomConfigurations(customConfigurations);

        return repository;
    }

}
