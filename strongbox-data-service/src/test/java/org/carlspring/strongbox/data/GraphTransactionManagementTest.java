package org.carlspring.strongbox.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import javax.inject.Inject;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.gremlin.graph.OrientGraphFactory;
import org.carlspring.strongbox.test.domain.FooEntity;
import org.carlspring.strongbox.test.service.TransactionalTestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = { DataServiceConfig.class,
                                  GraphTransactionManagementTest.GraphTransactionManagementTestConfiguration.class })
@Execution(SAME_THREAD)
@Commit
public class GraphTransactionManagementTest
{

    @Inject
    private TransactionalTestService testService;

    @Test
    public void testElementsCrud()
    {
        assertThatThrownBy(() -> OrientGraphFactory.getGraph()).isInstanceOf(IllegalStateException.class);

        testService.createVertexWithCommit();
        assertThat(testService.countVertices()).isEqualTo(Long.valueOf(1));

        assertThatThrownBy(() -> testService.createVerticesWithException()).isInstanceOf(RuntimeException.class);
        // XXX: `Neo4jTransactionManager` don't support
        // `Propagation.REQUIRES_NEW`
        assertThat(testService.countVertices()).isEqualTo(Long.valueOf(1));

        FooEntity entity = testService.createObjectWithCommit();
        assertThat(entity).matches(e -> e.getObjectId() != null);
        assertThat(testService.countObjects()).isEqualTo(Long.valueOf(1));
        
        assertThatThrownBy(() -> testService.createObjectWithException()).isInstanceOf(RuntimeException.class);
        assertThat(testService.countObjects()).isEqualTo(Long.valueOf(1));
        
        entity.setName("update-test");
        entity = testService.updateWithCommit(entity);
        assertThat(entity.getName()).isEqualTo("update-test");
        
        entity = testService.findById(entity.getUuid());
        assertThat(entity.getName()).isEqualTo("update-test");
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
