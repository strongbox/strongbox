package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.aws.MutableAwsConfiguration;
import org.carlspring.strongbox.storage.repository.gcs.MutableGoogleCloudConfiguration;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author carlspring
 */
public class RepositoryTest
{


    @Test
    public void testAddRepositoryWithCustomConfiguration()
            throws JAXBException
    {
        MutableRepository repository = createTestRepositoryWithCustomConfig();

        MutableStorage storage = new MutableStorage("storage0");
        storage.addRepository(repository);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        GenericParser<MutableConfiguration> parser = new GenericParser<>(MutableConfiguration.class);

        String serialized = parser.serialize(configuration);

        System.out.println(serialized);

        assertTrue(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertTrue(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
    }

    @Test
    public void testMarshallAndUnmarshallSimpleConfiguration()
            throws JAXBException
    {
        MutableStorage storage = new MutableStorage("storage0");

        MutableRepository repository = new MutableRepository("test-repository");
        repository.setStorage(storage);

        storage.addRepository(repository);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        GenericParser<MutableConfiguration> parser = new GenericParser<>(MutableConfiguration.class);

        String serialized = parser.serialize(configuration);

        System.out.println(serialized);

        assertFalse(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertFalse(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));

        MutableConfiguration unmarshalledConfiguration = parser.parse(new ByteArrayInputStream(serialized.getBytes()));

        assertNotNull(unmarshalledConfiguration.getStorage("storage0"));
    }

    @Test
    public void testMarshallAndUnmarshallSimpleConfigurationWithoutServiceLoader()
            throws JAXBException
    {
        MutableStorage storage = new MutableStorage("storage0");

        MutableRepository repository = new MutableRepository("test-repository");
        repository.setStorage(storage);

        storage.addRepository(repository);

        MutableConfiguration configuration = new MutableConfiguration();
        configuration.addStorage(storage);

        GenericParser<MutableConfiguration> parser = new GenericParser<>(false,
                                                                         MutableConfiguration.class,
                                                                         MutableCustomConfiguration.class);

        String serialized = parser.serialize(configuration);

        System.out.println(serialized);

        assertFalse(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertFalse(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));

        MutableConfiguration unmarshalledConfiguration = parser.parse(new ByteArrayInputStream(serialized.getBytes()));

        assertNotNull(unmarshalledConfiguration.getStorage("storage0"));
    }

    @Test
    public void testMarshallAndUnmarshallStrongboxConfiguration()
            throws JAXBException, IOException
    {
        File file = new File("target/strongbox/etc/conf/strongbox.xml");

        GenericParser<MutableConfiguration> parser = new GenericParser<>(MutableConfiguration.class);
        MutableConfiguration configuration = parser.parse(file.toURI().toURL());

        MutableStorage storage = configuration.getStorage("storage0");

        assertNotNull(storage);
    }

    private MutableRepository createTestRepositoryWithCustomConfig()
    {
        MutableStorage storage = new MutableStorage("storage0");
        MutableRepository repository = new MutableRepository("test-repository");
        repository.setStorage(storage);

        MutableAwsConfiguration awsConfiguration = new MutableAwsConfiguration();
        awsConfiguration.setBucket("test-bucket");
        awsConfiguration.setKey("test-key");

        MutableGoogleCloudConfiguration googleCloudConfiguration = new MutableGoogleCloudConfiguration();
        googleCloudConfiguration.setBucket("test-bucket");
        googleCloudConfiguration.setKey("test-key");

        List<MutableCustomConfiguration> customConfigurations = new ArrayList<>();
        customConfigurations.add(awsConfiguration);
        customConfigurations.add(googleCloudConfiguration);

        repository.setCustomConfigurations(customConfigurations);

        return repository;
    }

}
