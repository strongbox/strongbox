package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.validation.version.MavenReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.version.MavenSnapshotVersionValidator;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StorageApiTestConfig.class)
public class DefaultMavenArtifactCoordinateValidatorsTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    public static final String CONFIGURATION_OUTPUT_FILE = CONFIGURATION_BASEDIR + "/strongbox-saved-cm.xml";

    private GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ArtifactResolutionService artifactResolutionService;


    @Before
    public void setUp()
            throws Exception
    {
        File xmlDir = new File(CONFIGURATION_BASEDIR);
        if (!xmlDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            xmlDir.mkdirs();
        }
    }

    @Test
    public void testParseConfiguration()
    {
        final Configuration configuration = configurationManager.getConfiguration();

        assertNotNull(configuration);
        assertNotNull(configuration.getStorages());
        assertNotNull(configuration.getRoutingRules());

        for (String storageId : configuration.getStorages().keySet())
        {
            assertNotNull("Storage ID was null!", storageId);
        }

        assertTrue("Unexpected number of storages!", configuration.getStorages().size() > 0);
        assertNotNull("Incorrect version!", configuration.getVersion());
        assertEquals("Incorrect port number!", 48080, configuration.getPort());
        assertTrue("Repository should have required authentication!",
                   configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("snapshots")
                                .isSecured());

        assertTrue(configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("releases")
                                .allowsDirectoryBrowsing());

        Set<String> versionValidators = configuration.getStorages()
                                                     .get("storage0")
                                                     .getRepositories()
                                                     .get("releases")
                                                     .getArtifactCoordinateValidators();

        assertTrue(versionValidators.size() == 2);
        assertTrue(versionValidators.remove(MavenSnapshotVersionValidator.ALIAS));
        assertTrue(versionValidators.remove(MavenReleaseVersionValidator.ALIAS));
        assertTrue(versionValidators.size() == 0);
    }

}
