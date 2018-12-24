package org.carlspring.strongbox.authentication;

import org.carlspring.strongbox.config.UsersConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ UsersConfig.class })
public class TestConfig
{



}
