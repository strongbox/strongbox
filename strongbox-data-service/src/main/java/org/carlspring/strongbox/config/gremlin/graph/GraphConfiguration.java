package org.carlspring.strongbox.config.gremlin.graph;

import org.springframework.context.annotation.Bean;

import com.orientechnologies.orient.core.db.ODatabasePool;

@org.springframework.context.annotation.Configuration
public class GraphConfiguration
{

    @Bean
    OrientGraphFactory graphFactory(ODatabasePool databasePool)
    {
        return new OrientGraphFactory(databasePool);
    }

}
