package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ResourcesBooterTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    // This field is indeed used. It's execute() method is being invoked with a @PostConstruct.
    @Inject
    private ResourcesBooter resourcesBooter;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        // No need to clean up anything, just overriding the method from the parent
    }

    @Test
    public void testResourceBooting()
            throws Exception
    {
        File file = new File(ConfigurationResourceResolver.getHomeDirectory() + "/etc/conf/strongbox.xml");

        assertTrue(file.exists(), "Failed to copy configuration resource from classpath!");
    }

}
