package org.carlspring.strongbox.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.carlspring.strongbox.test.service.TransactionalTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    @TransactionContext
    private Graph graph;

    @Test
    public void testElementsCrud()
    {
        assertThatThrownBy(() -> graph.traversal()).isInstanceOf(IllegalStateException.class);

        testService.createVertexWithCommit();
        assertThat(testService.countVertices()).isEqualTo(Long.valueOf(1));

        assertThatThrownBy(() -> testService.createVerticesWithException()).isInstanceOf(RuntimeException.class);
        // XXX: `Neo4jTransactionManager` don't support
        // `Propagation.REQUIRES_NEW`
        assertThat(testService.countVertices()).isEqualTo(Long.valueOf(1));
    }

    @Configuration
    static class GraphTransactionManagementTestConfiguration
    {

        @Bean
        public TransactionalTestService testService()
        {
            return new TransactionalTestService();
        }

    }

}
