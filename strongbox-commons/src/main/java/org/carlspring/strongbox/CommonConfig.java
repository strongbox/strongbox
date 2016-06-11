package org.carlspring.strongbox;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.io",
        "org.carlspring.strongbox.net",
        "org.carlspring.strongbox.db",
        "org.carlspring.strongbox.resource",
        "org.carlspring.strongbox.rest",
        "org.carlspring.strongbox.storage.repository",
        "org.carlspring.strongbox.url",
        "org.carlspring.strongbox.util",
        "org.carlspring.strongbox.xml"
})
public class CommonConfig
{
}
