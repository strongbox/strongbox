package org.carlspring.strongbox.config.janusgraph;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.strongbox.db.server.CassandraEmbeddedProperties;

@ConstructorBinding
@ConfigurationProperties(prefix = "strongbox.db.cassandra")
public class StrongboxCassandraEmbeddedProperties extends CassandraEmbeddedProperties
{

    public StrongboxCassandraEmbeddedProperties(String storageRoot,
                                                String configLocation)
    {
        super(storageRoot, configLocation);
    }

}
