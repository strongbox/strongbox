package org.carlspring.strongbox.app;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.config.janusgraph.JanusGraphDbProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
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

    private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args)
    {
        if (System.getProperty(JanusGraphDbProfile.PROPERTY_PROFILE) == null)
        {
            logger.info("JanusGraphDb profile not set, will use [{}] profile as default",
                        JanusGraphDbProfile.PROFILE_EMBEDDED);

            System.setProperty(JanusGraphDbProfile.PROPERTY_PROFILE, JanusGraphDbProfile.PROFILE_EMBEDDED);
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
}
