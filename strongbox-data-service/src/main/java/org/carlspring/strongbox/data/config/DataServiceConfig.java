package org.carlspring.strongbox.data.config;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.carlspring.strongbox.data.domain.StrongboxUser;
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

import javax.annotation.PostConstruct;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@EnableTransactionManagement
@EnableOrientRepositories(basePackages = "org.carlspring.strongbox.data.repository")
@ComponentScan({"org.carlspring.strongbox.data"})
@Import(DataServicePropertiesConfig.class)
public class DataServiceConfig {

    private final String DOMAIN_PACKAGE = StrongboxUser.class.getPackage().getName();

    @Value("${org.carlspring.strongbox.data.orientdb.host}")
    String host;

    @Value("${org.carlspring.strongbox.data.orientdb.database}")
    String database;

    @Value("${org.carlspring.strongbox.data.orientdb.user}")
    String user;

    @Value("${org.carlspring.strongbox.data.orientdb.password}")
    String password;

    public DataServiceConfig(){

        // create database if not initialized
        ODatabaseDocumentTx db = new ODatabaseDocumentTx(getConnectionUrl());
        if (!db.exists()) {
            db.create();
            db.close();
        }
    }

    @Bean
    public OrientObjectDatabaseFactory factory() {
        OrientObjectDatabaseFactory factory = new OrientObjectDatabaseFactory();
        factory.setUrl(getConnectionUrl());
        factory.setUsername(user);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public OrientTransactionManager transactionManager() {
        return new OrientTransactionManager(factory());
    }

    @Bean
    public OrientObjectTemplate objectTemplate() {
        return new OrientObjectTemplate(factory());
    }

    @PostConstruct
    public void registerEntities() {

        // register all domain entities
        factory().db().getEntityManager().registerEntityClasses(DOMAIN_PACKAGE);
    }

    private final String getConnectionUrl(){
        return "plocal:data/" + database;
    }
}
