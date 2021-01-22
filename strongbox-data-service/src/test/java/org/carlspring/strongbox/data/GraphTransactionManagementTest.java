package org.carlspring.strongbox.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.carlspring.strongbox.repositories.TransactionalIdBlockQueueSuppiler;
import org.carlspring.strongbox.test.service.IdBlockQueueTestService;
import org.carlspring.strongbox.test.service.TransactionalTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("/application.yaml")
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  GraphTransactionManagementTest.GraphTransactionManagementTestConfiguration.class })
@Execution(SAME_THREAD)
@Commit
public class GraphTransactionManagementTest
{

    @Inject
    private TransactionalTestService testService;
    @Inject
    private IdBlockQueueTestService idBlockQueueTestService;
    @Inject
    @TransactionContext
    private Graph graph;

    @Test
    public void testElementsCrud()
    {
        Long count = testService.countVertices();
        assertThatThrownBy(() -> graph.traversal()).isInstanceOf(IllegalStateException.class);

        testService.createVertexWithCommit();
        assertThat(testService.countVertices()).isEqualTo(count + 1);

        assertThatThrownBy(() -> testService.createVerticesWithException()).isInstanceOf(RuntimeException.class);
        // XXX: `Neo4jTransactionManager` don't support
        // `Propagation.REQUIRES_NEW`
        assertThat(testService.countVertices()).isEqualTo(count + 1);
    }

    @Test
    public void testIdBlockQueue()
    {
        for (int i = 0; i < 10; i++)
        {
            Long firstId = idBlockQueueTestService.createWithGtmtTransactionManagerFirst();
            Long secondId = idBlockQueueTestService.createWithgtmtTransactionManagerSecond();
            //we have `ids.block-size=1000`, so IDs from different queues must differ by the size of one block
            assertThat(secondId).isEqualTo(firstId + 1000);
        }
    }


    @Configuration
    static class GraphTransactionManagementTestConfiguration
    {

        @Bean
        public TransactionalTestService testService()
        {
            return new TransactionalTestService();
        }

        @Bean
        public IdBlockQueueTestService idBlockQueueTestService()
        {
            return new IdBlockQueueTestService();
        }


        @Bean
        @Qualifier("gtmtTransactionManagerFirst")
        public Neo4jTransactionManager gtmtTransactionManagerFirst(TransactionalIdBlockQueueSuppiler idBlockQueueSessionFactory)
            throws Exception
        {
            return new Neo4jTransactionManager(idBlockQueueSessionFactory.getSessionFactory("gtmtTransactionManagerFirst"));
        }

        @Bean
        @Qualifier("gtmtTransactionManagerSecond")
        public Neo4jTransactionManager gtmtTransactionManagerSecond(TransactionalIdBlockQueueSuppiler idBlockQueueSessionFactory)
            throws Exception
        {
            return new Neo4jTransactionManager(idBlockQueueSessionFactory.getSessionFactory("gtmtTransactionManagerSecond"));
        }
    }

}
