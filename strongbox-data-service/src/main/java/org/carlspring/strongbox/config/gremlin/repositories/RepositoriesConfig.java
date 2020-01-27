package org.carlspring.strongbox.config.gremlin.repositories;

import org.carlspring.strongbox.config.gremlin.graph.OrientGraphFactory;
import org.neo4j.ogm.session.SessionFactory;
import org.opencypher.gremlin.neo4j.ogm.OrientDbGraphDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;

@Configuration
@EnableNeo4jRepositories
public class RepositoriesConfig
{

    @Bean
    public SessionFactory sessionFactory(OrientGraphFactory graphFactory)
    {
        return new SessionFactory(new OrientDbGraphDriver(graphFactory), "org.carlspring.strongbox.domain");
    }

    @Bean
    public Neo4jTransactionManager transactionManager(SessionFactory sessionFactory)
        throws Exception
    {
        return new Neo4jTransactionManager(sessionFactory);
    }

}
