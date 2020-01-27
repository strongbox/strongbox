package org.carlspring.strongbox.config.janusgraph;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.strongbox.db.server.JanusGraphProperties;

@ConstructorBinding
@ConfigurationProperties(prefix = "strongbox.db.janusgraph")
public class StrongboxJanusGraphProperties extends JanusGraphProperties
{

    public StrongboxJanusGraphProperties(String configLocation)
    {
        super(configLocation);
    }
}
