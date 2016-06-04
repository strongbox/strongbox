package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author korest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProxyRepositoryConnectionPoolConfigurationManagmentRestletTest extends TestCaseWithArtifactGeneration {
    @Configuration
    @ComponentScan(basePackages = {"org.carlspring.strongbox"})
    public static class SpringConfig { }

    @Test
    public void setNumberOfConnectionsForProxyRepositoryTest()
    {

    }
}
