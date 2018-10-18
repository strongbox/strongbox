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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { Maven2LayoutProviderTestConfig.class})
public class DefaultMavenArtifactCoordinateValidatorsTest
{

    public static final String TEST_CLASSES = "target/test-classes";

    public static final String CONFIGURATION_BASEDIR = TEST_CLASSES + "/xml";

    @Inject
    private ConfigurationManager configurationManager;


    @BeforeEach
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
            assertNotNull(storageId, "Storage ID was null!");
        }

        assertTrue(configuration.getStorages().size() > 0, "Unexpected number of storages!");
        assertNotNull(configuration.getVersion(), "Incorrect version!");
        assertEquals(48080, configuration.getPort(), "Incorrect port number!");
        assertTrue(configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("snapshots")
                                .isSecured(),
                   "Repository should have required authentication!");

        assertTrue(configuration.getStorages()
                                .get("storage0")
                                .getRepositories()
                                .get("releases")
                                .allowsDirectoryBrowsing());

        Set<String> versionValidators = configuration.getStorages()
                                                     .get("storage0")
                                                     .getRepositories()
                                                     .get("releases")
                                                     .getArtifactCoordinateValidators()
                                                     .keySet();

        assertFalse(versionValidators.isEmpty());
        assertTrue(versionValidators.contains(RedeploymentValidator.ALIAS));
        assertTrue(versionValidators.contains(MavenSnapshotVersionValidator.ALIAS));
        assertTrue(versionValidators.contains(MavenReleaseVersionValidator.ALIAS));
    }

}
