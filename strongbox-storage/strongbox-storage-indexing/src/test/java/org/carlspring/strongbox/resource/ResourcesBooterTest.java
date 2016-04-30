package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.ResourcesBooter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ResourcesBooterTest
{

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = {"org.carlspring.strongbox", "org.carlspring.logging"})
    public static class SpringConfig { }

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
