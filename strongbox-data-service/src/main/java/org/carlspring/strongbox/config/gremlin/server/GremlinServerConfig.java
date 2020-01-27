package org.carlspring.strongbox.config.gremlin.server;

import java.io.InputStream;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphBaseFactory;
import org.apache.tinkerpop.gremlin.server.GremlinServer;
import org.apache.tinkerpop.gremlin.server.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@Configuration
@ConditionalOnProperty(prefix = "strongbox.graph.gremlin.server", name = "enabled", havingValue = "true")
public class GremlinServerConfig
{

    @Bean(destroyMethod = "stop")
    GremlinServer gremlinServer(OrientGraphBaseFactory graphFactory,
                                @Value("classpath:/gremlin/gremlin-server.yaml")
                                Resource gremlinServerConf)
            throws Exception
    {
        GremlinServer server;
        try (InputStream inputStream = gremlinServerConf.getInputStream())
        {
            Settings settings = Settings.read(inputStream);
            server = new GremlinServer(settings);
        }

        OrientGraph graph = graphFactory.getNoTx();
        server.getServerGremlinExecutor().getGraphManager().putGraph("graph", graph);
        server.getServerGremlinExecutor().getGremlinExecutor().getScriptEngineManager().put("g", graph.traversal());

        server.start().join();
        return server;
    }

}
