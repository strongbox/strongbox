package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.ResourcesBooter;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ResourcesBooterTest extends TestCaseWithArtifactGenerationWithIndexing
{
    private static final Logger logger = LoggerFactory.getLogger(ResourcesBooterTest.class);
    
    // This field is indeed used. It's execute() method is being invoked with a @PostConstruct.
    @Autowired
    private ResourcesBooter resourcesBooter;

    @Test
    public void testResourceBooting()
            throws Exception
    {
        File file = new File(ConfigurationResourceResolver.getVaultDirectory() + "/etc/conf/strongbox.xml");

        assertTrue("Failed to copy configuration resource from classpath!", file.exists());
    }

}
