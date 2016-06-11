package org.carlspring.strongbox;

import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class BaseStorageApiTest extends TestCaseWithArtifactGeneration
{

    @org.springframework.context.annotation.Configuration
    @Import({
            StorageApiConfig.class,
            CommonConfig.class
    })
    public static class SpringConfig { }

}
