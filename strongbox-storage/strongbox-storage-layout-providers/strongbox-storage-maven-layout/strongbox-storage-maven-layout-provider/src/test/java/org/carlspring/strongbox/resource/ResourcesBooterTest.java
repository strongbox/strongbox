package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ResourcesBooterTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    // This field is indeed used. It's execute() method is being invoked with a @PostConstruct.
    @Inject
    private ResourcesBooter resourcesBooter;

    @Inject
    private PropertiesBooter propertiesBooter;


    @BeforeAll
    public static void cleanUp()
    {
        // No need to clean up anything, just overriding the method from the parent
    }

    @Test
    public void testResourceBooting()
    {
        File file = new File(propertiesBooter.getHomeDirectory() + "/etc/conf/strongbox.xml");

        assertTrue(file.exists(), "Failed to copy configuration resource from classpath!");
    }

}
