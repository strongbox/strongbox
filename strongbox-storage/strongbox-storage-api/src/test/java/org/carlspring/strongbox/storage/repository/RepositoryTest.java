package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
public class RepositoryTest
{

/*
    @org.springframework.context.annotation.Configuration
    @Import({ StorageApiConfig.class,
              CommonConfig.class,
              ClientConfig.class,
              DataServiceConfig.class
            })
    public static class SpringConfig { }

    @Autowired
    private ConfigurationManager configurationManager;
*/


    @Test
    public void testRepositoryWithCustomConfiguration()
            throws JAXBException
    {
        Repository repository = createTestRepository();

        GenericParser<Repository> parser = new GenericParser<>(Repository.class,
                                                               AwsConfiguration.class,
                                                               GoogleCloudConfiguration.class);
        String serialized = parser.serialize(repository);

        System.out.println(serialized);

        assertTrue(serialized.contains("<aws-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
        assertTrue(serialized.contains("<google-cloud-configuration bucket=\"test-bucket\" key=\"test-key\"/>"));
    }

    @Test
    public void testAddRepositoryWithCustomConfiguration()
    {
        Repository repository = createTestRepository();

        // configurationManager.getConfiguration().getStorage("storage0").addOrUpdateRepository(repository);
    }

    private Repository createTestRepository()
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
