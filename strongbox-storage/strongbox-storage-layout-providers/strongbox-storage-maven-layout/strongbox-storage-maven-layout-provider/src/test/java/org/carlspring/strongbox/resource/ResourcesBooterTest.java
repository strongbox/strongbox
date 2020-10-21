package org.carlspring.strongbox.resource;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;

import javax.inject.Inject;
import java.io.File;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ResourcesBooterTest
{

    @Inject
    private PropertiesBooter propertiesBooter;

    @Test
    public void testResourceBooting()
    {
        File file = new File(propertiesBooter.getHomeDirectory() + "/etc/conf/strongbox.yaml");

        assertThat(file.exists()).as("Failed to copy configuration resource from classpath!").isTrue();
    }

}
