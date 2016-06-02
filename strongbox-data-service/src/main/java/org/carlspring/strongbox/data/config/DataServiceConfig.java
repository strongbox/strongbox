package org.carlspring.strongbox.data.config;

import org.carlspring.strongbox.data.domain.StrongboxUser;
import org.carlspring.strongbox.data.tx.CustomOrientTransactionManager;

import javax.annotation.PostConstruct;
import java.io.IOException;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.orient.commons.core.OrientTransactionManager;
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;
import org.springframework.data.orient.object.OrientObjectTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@EnableTransactionManagement
@EnableOrientRepositories(basePackages = "org.carlspring.strongbox.data.repository")
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import(DataServicePropertiesConfig.class)
public class DataServiceConfig
{

    private static final Logger logger = LoggerFactory.getLogger(DataServiceConfig.class);

    private final String DOMAIN_PACKAGE = StrongboxUser.class.getPackage().getName();

    @Value("${org.carlspring.strongbox.data.orientdb.host}")
    String host;

    @Value("${org.carlspring.strongbox.data.orientdb.port}")
    Integer port;

    @Value("${org.carlspring.strongbox.data.orientdb.database}")
    String database;

    @Value("${org.carlspring.strongbox.data.orientdb.user}")
    String user;

    @Value("${org.carlspring.strongbox.data.orientdb.password}")
    String password;

    @Bean
    public OrientObjectDatabaseFactory factory()
    {
        OrientObjectDatabaseFactory factory = new OrientObjectDatabaseFactory();
        factory.setUrl(getConnectionUrl());
        factory.setUsername(user);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public OrientTransactionManager transactionManager()
    {
        return new CustomOrientTransactionManager(factory());
    }

    @Bean
    public OrientObjectTemplate objectTemplate()
    {
        return new OrientObjectTemplate(factory());
    }

    @PostConstruct
    public void registerEntities()
            throws IOException
    {

        // create database if not initialized
        OServerAdmin serverAdmin = new OServerAdmin(getConnectionUrl()).connect("root", "root");
        if (!serverAdmin.existsDatabase())
        {
            logger.debug("Create database " + database);
            serverAdmin.createDatabase(database, "document", "plocal").close();
        }

        // register all domain entities
        factory().db().getEntityManager().registerEntityClasses(DOMAIN_PACKAGE);
    }

    private String getConnectionUrl()
    {
        return "remote:" + host + ":" + port + "/" + database;
    }

}
