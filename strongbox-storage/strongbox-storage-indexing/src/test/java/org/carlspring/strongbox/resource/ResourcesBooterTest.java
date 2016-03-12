package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.ResourcesBooter;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ResourcesBooterTest
{

    // This field is indeed used. It's execute() method is being invoked with a @PostConstruct.
    @SuppressWarnings("UnusedDeclaration")
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
