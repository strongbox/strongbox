package org.carlspring.strongbox;

import org.carlspring.strongbox.testing.AssignedPorts;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TestingCoreConfig.
 */
@Configuration
public class TestingCoreConfig
{

    @Bean(name = "assignedPorts")
    AssignedPorts assignedPorts()
    {
        return new AssignedPorts();
    }

}
