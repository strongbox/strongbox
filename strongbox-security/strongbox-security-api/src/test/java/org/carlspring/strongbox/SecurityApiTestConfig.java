package org.carlspring.strongbox;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Przemyslaw Fusik
 */

@Configuration
@ComponentScan(basePackages = { "org.carlspring.strongbox.security",
                                "org.carlspring.strongbox.testing" })
public class SecurityApiTestConfig
{

}
