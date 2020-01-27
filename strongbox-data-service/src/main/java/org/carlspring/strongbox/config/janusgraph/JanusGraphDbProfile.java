package org.carlspring.strongbox.config.janusgraph;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class JanusGraphDbProfile
{

    private static final Logger logger = LoggerFactory.getLogger(JanusGraphDbProfile.class);

    public static final String PROPERTY_PROFILE = "strongbox.db.janus-graph.profile";

    public static final String PROFILE_MEMORY = "janusgraph_MEMORY";

    public static final String PROFILE_EMBEDDED = "janusgraph_EMBEDDED";

    public static final String PROFILE_REMOTE = "janusgraph_REMOTE";

    private final String name;

    public JanusGraphDbProfile(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static JanusGraphDbProfile resolveProfile(Environment environment)
    {
        try
        {
            bootstrap();
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        String profile = environment.getProperty(PROPERTY_PROFILE, PROFILE_MEMORY);

        return new JanusGraphDbProfile(profile);
    }

    public static void bootstrap()
        throws IOException
    {
        String profile = System.getProperty(PROPERTY_PROFILE, PROFILE_MEMORY);

        logger.info("Bootstrap JanusGraph connection properties with profile [{}].", profile);

        try (InputStream is = JanusGraphDbProfile.class.getResourceAsStream(String.format("/META-INF/properties/%s.properties",
                                                                                          profile)))
        {
            Properties properties = new Properties();
            properties.load(is);

            properties.keySet()
                      .stream()
                      .forEach(p -> {
                          if (System.getProperty((String) p) != null)
                          {
                              return;
                          }

                          logger.debug("Using default value for JanusGraph connection property [{}].", p);

                          System.setProperty((String) p, properties.getProperty((String) p));
                      });
        }

    }

}
