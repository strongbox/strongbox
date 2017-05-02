package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ResourcesBooterTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    // This field is indeed used. It's execute() method is being invoked with a @PostConstruct.
    @Inject
    private ResourcesBooter resourcesBooter;


    @BeforeClass
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

        assertTrue("Failed to copy configuration resource from classpath!", file.exists());
    }

}
