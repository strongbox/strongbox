package org.carlspring.strongbox.config;

import org.carlspring.strongbox.testing.AssignedPorts;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TestingCoreConfig.
 */
@Configuration
public class TestingCoreConfig
{

    @Bean(name = "assignedPorts")
    protected AssignedPorts assignedPorts()
    {
        return new AssignedPorts();
    }

    @Bean
    protected YAMLMapperFactory yamlMapperFactory()
    {
        return TestingYamlMapper::new;
    }

}
