package org.carlspring.strongbox;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGeneration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class BaseStorageApiTest extends TestCaseWithMavenArtifactGeneration
{

    @org.springframework.context.annotation.Configuration
    @Import({
                    StorageCoreConfig.class,
                    CommonConfig.class,
                    ClientConfig.class
    })
    public static class SpringConfig { }

}
