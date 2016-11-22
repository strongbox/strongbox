package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
public class RepositoryTest
{


    @org.springframework.context.annotation.Configuration
    @Import({ StorageApiConfig.class,
              CommonConfig.class,
              ClientConfig.class,
              DataServiceConfig.class
            })
    public static class SpringConfig { }

    @Autowired
    private ConfigurationManager configurationManager;


    @Test
    public void testRepositoryWithCustomConfiguration()
            throws JAXBException
    {
        Repository repository = createTestRepository();

        GenericParser<Repository> parser = new GenericParser<>(Repository.class);
        String serialized = parser.serialize(repository);

        System.out.println(serialized);

        assertTrue(serialized.contains("<customConfiguration>\n" +
                                       "        <entry>\n" +
                                       "            <key>strongbox-storage-aws.aws.accessKey</key>\n" +
                                       "            <value>ACCESSKEY</value>\n" +
                                       "        </entry>\n" +
                                       "        <entry>\n" +
                                       "            <key>strongbox-storage-aws.aws.s3.unique-prefix-for-bucket</key>\n" +
                                       "            <value>strongbox-test-</value>\n" +
                                       "        </entry>\n" +
                                       "        <entry>\n" +
                                       "            <key>strongbox-storage-aws.aws.secretKey</key>\n" +
                                       "            <value>SECRETKEY</value>\n" +
                                       "        </entry>\n" +
                                       "        <entry>\n" +
                                       "            <key>strongbox-storage-aws.aws.s3.region</key>\n" +
                                       "            <value>eu-west-1</value>\n" +
                                       "        </entry>\n" +
                                       "    </customConfiguration>"));
    }

    @Test
    public void testAddRepositoryWithCustomConfiguration()
    {
        Repository repository = createTestRepository();

        configurationManager.getConfiguration().getStorage("storage0").addOrUpdateRepository(repository);
    }

    private Repository createTestRepository()
    {
        Storage storage = new Storage("storage0");
        Repository repository = new Repository("test-repository");
        repository.setStorage(storage);

        Map<String, String> customConfiguration = new HashMap<>();
        customConfiguration.put("strongbox-storage-aws.aws.accessKey", "ACCESSKEY");
        customConfiguration.put("strongbox-storage-aws.aws.secretKey", "SECRETKEY");
        customConfiguration.put("strongbox-storage-aws.aws.s3.region", "eu-west-1");
        customConfiguration.put("strongbox-storage-aws.aws.s3.unique-prefix-for-bucket", "strongbox-test-");

        repository.setCustomConfiguration(customConfiguration);

        return repository;
    }

}
