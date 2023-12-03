package org.carlspring.strongbox.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.artifact",
                 "org.carlspring.strongbox.configuration",
                 "org.carlspring.strongbox.io",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.resource",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
                 "org.carlspring.strongbox.yaml"
               })
public class StorageCoreConfig
{

}
