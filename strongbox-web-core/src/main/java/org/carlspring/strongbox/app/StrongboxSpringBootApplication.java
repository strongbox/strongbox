package org.carlspring.strongbox.app;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.config.ConnectionConfigOrientDB;
import org.carlspring.strongbox.config.WebConfig;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * @author carlspring
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,
                                   HibernateJpaAutoConfiguration.class })
@Import(WebConfig.class)
public class StrongboxSpringBootApplication
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxSpringBootApplication.class);

    public static void main(String[] args)
            throws IOException
    {
        //PropertiesBooter.initialize();
        StorageBooter.createTempDir();

        if (System.getProperty(ConnectionConfigOrientDB.PROPERTY_PROFILE) == null)
        {
            logger.info(String.format("OrientDB profile not set, will use [%s] profile as default",
                                      ConnectionConfigOrientDB.PROFILE_EMBEDDED));

            System.setProperty(ConnectionConfigOrientDB.PROPERTY_PROFILE, ConnectionConfigOrientDB.PROFILE_EMBEDDED);
        }

        ConfigurableApplicationContext applicationContext = SpringApplication.run(StrongboxSpringBootApplication.class, args);
        applicationContext.start();
    }

}
