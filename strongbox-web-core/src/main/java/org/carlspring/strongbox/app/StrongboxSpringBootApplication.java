package org.carlspring.strongbox.app;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.OrientDBProfile;
import org.carlspring.strongbox.config.WebConfig;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author carlspring
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,
                                   HibernateJpaAutoConfiguration.class })
@Import({ WebConfig.class,
          StrongboxSpringBootApplication.InitializationConfig.class })
public class StrongboxSpringBootApplication
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxSpringBootApplication.class);

    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args)
    {
        if (System.getProperty(OrientDBProfile.PROPERTY_PROFILE) == null)
        {
            logger.info(String.format("OrientDB profile not set, will use [%s] profile as default",
                                      OrientDBProfile.PROFILE_EMBEDDED));

            System.setProperty(OrientDBProfile.PROPERTY_PROFILE, OrientDBProfile.PROFILE_EMBEDDED);
        }

        applicationContext = SpringApplication.run(StrongboxSpringBootApplication.class, args);
        applicationContext.start();
    }

    public static void restart()
    {
        ApplicationArguments args = applicationContext.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            applicationContext.close();
            applicationContext = SpringApplication.run(StrongboxSpringBootApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }

    @Configuration
    static class InitializationConfig
    {

        @Inject
        private PropertiesBooter propertiesBooter;

        @PostConstruct
        void init()
        {
            System.setProperty("strongbox.storage.booter.basedir", propertiesBooter.getStorageBooterBasedir());
        }
    }

}
