package org.carlspring.strongbox.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * @author carlspring
 */
@EnableAutoConfiguration(exclude={ DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@SpringBootApplication
public class StrongboxSpringBootApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(StrongboxSpringBootApplication.class, args);
    }

}