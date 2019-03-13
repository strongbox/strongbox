package org.carlspring.strongbox.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author kalski
 */
@Configuration
@Import(ClientConfig.class)
@ComponentScan({ "org.carlspring.strongbox.booters" })
public class NugetBootersTestConfig
{
}
