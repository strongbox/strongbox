package org.carlspring.strongbox.storage.validation;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.validation.deployment.RedeploymentValidator;
import org.carlspring.strongbox.storage.validation.version.MavenReleaseVersionValidator;
import org.carlspring.strongbox.storage.validation.version.MavenSnapshotVersionValidator;

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
@ContextConfiguration(classes = { Maven2LayoutProviderTestConfig.class})
public class DefaultMavenArtifactCoordinateValidatorsTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    @Inject
    private ConfigurationManager configurationManager;


    @Before
    public void setUp()
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

        assertFalse(versionValidators.isEmpty());
        assertTrue(versionValidators.contains(RedeploymentValidator.ALIAS));
        assertTrue(versionValidators.contains(MavenSnapshotVersionValidator.ALIAS));
        assertTrue(versionValidators.contains(MavenReleaseVersionValidator.ALIAS));
    }

}
