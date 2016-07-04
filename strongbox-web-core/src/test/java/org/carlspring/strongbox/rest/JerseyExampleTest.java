package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebConfig.class })
@Commit
public class JerseyExampleTest extends JerseyTest
{

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }

    @Override
    public Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        forceSet(TestProperties.CONTAINER_PORT, "0");
        set(TestProperties.RECORD_LOG_LEVEL, 10);
        return new ResourceConfig(UserRestlet.class);
    }

    @Test
    public void simplestTest(){
        System.out.println("OK");
    }

    @Test
    //@WithMockUser(username="admin", roles={"ADMIN"})
    public synchronized void test() {

        System.out.println("test");
        try
        {
            final String hello = target("/users/greet").request().get(String.class);
            System.out.println(hello);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
