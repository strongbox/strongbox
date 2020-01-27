package org.carlspring.strongbox.config.janusgraph;

import org.carlspring.strongbox.db.schema.StrongboxSchema;
import org.janusgraph.core.JanusGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.strongbox.db.server.EmbeddedDbServer;
import org.strongbox.db.server.EmbeddedJanusGraphWithCassandraServer;
import org.strongbox.db.server.InMemoryJanusGraphServer;
import org.strongbox.db.server.JanusGraphConfiguration;
import org.strongbox.db.server.JanusGraphProperties;

/**
 * @author sbespalov
 */
@Configuration
@Conditional(InMemoryDbServerConfiguration.class)
public class InMemoryDbServerConfiguration implements Condition
{

    private static final Logger logger = LoggerFactory.getLogger(InMemoryDbServerConfiguration.class);

    @Bean
    EmbeddedDbServer embeddedDbServer(JanusGraphConfiguration janusGraphConfiguration)
    {

        return new InMemoryJanusGraphServer(janusGraphConfiguration);
    }

    @Bean
    JanusGraph JanusGraph(EmbeddedDbServer server)
        throws Exception
    {
        JanusGraph janusGraph = ((InMemoryJanusGraphServer) server).getJanusGraph();
        logger.info("Apply schema changes.");
        new StrongboxSchema().createSchema(janusGraph);
        logger.info("Schema changes applied.");

        return janusGraph;
    }
    
    @Bean
    @ConfigurationProperties(prefix = "strongbox.db.janus-graph")
    JanusGraphConfiguration janusGraphConfiguration()
    {
        return new JanusGraphProperties();
    }

    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        JanusGraphDbProfile profile = JanusGraphDbProfile.resolveProfile(conditionContext.getEnvironment());

        return profile.getName().equals(JanusGraphDbProfile.PROFILE_MEMORY);
    }

}
