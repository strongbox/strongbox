package org.carlspring.strongbox.server;

import org.carlspring.strongbox.config.ConnectionConfigOrientDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;

/**
 * @author carlspring
 */
@SpringBootApplication
@ComponentScan(basePackages = { "org.carlspring.strongbox", "com.carlspring.strongbox"})
@EnableAutoConfiguration(exclude={ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class StrongboxSpringBootApplication
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxSpringBootApplication.class);

    public static void main(String[] args)
    {
        if (System.getProperty(ConnectionConfigOrientDB.PROPERTY_PROFILE) == null)
        {
            logger.info(String.format("OrientDB profile not set, will use [%s] profile as default",
                                      ConnectionConfigOrientDB.PROFILE_EMBEDDED));

            System.setProperty(ConnectionConfigOrientDB.PROPERTY_PROFILE, ConnectionConfigOrientDB.PROFILE_EMBEDDED);
        }

        SpringApplication.run(StrongboxSpringBootApplication.class, args);
    }

}