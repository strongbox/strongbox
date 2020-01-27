package org.carlspring.strongbox.repositories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.gremlin.tx.GraphTransaction;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.janusgraph.core.JanusGraph;
import org.neo4j.ogm.session.SessionFactory;
import org.opencypher.gremlin.neo4j.ogm.GremlinGraphDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;

@Configuration
@ComponentScan
@EnableNeo4jRepositories
public class RepositoriesConfig
{

    @Bean
    public SessionFactory sessionFactory(JanusGraph graph)
    {
        return new SessionFactory(new GremlinGraphDriver(graph.tx()), "org.carlspring.strongbox.domain", "org.carlspring.strongbox.artifact.coordinates");
    }

    @Bean
    public Neo4jTransactionManager transactionManager(SessionFactory sessionFactory)
        throws Exception
    {
        return new Neo4jTransactionManager(sessionFactory);
    }

    @Bean
    @TransactionContext
    public Graph graphTransaction(SessionFactory sessionFactory)
    {
        return new GraphTransaction(sessionFactory);
    }

}
