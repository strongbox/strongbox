package org.carlspring.strongbox.config.orientdb;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class OrientDbProfile
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbProfile.class);

    public static final String PROPERTY_PROFILE = "strongbox.orientdb.profile";

    public static final String PROPERTY_STUDIO_ENABLED = "strongbox.orientdb.studio.enabled";
    
    public static final String PROFILE_MEMORY = "orientdb_MEMORY";

    public static final String PROFILE_EMBEDDED = "orientdb_EMBEDDED";

    public static final String PROFILE_REMOTE = "orientdb_REMOTE";

    private final String name;
    
    
    public OrientDbProfile(String name)
    {
        super();
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public static OrientDbProfile resolveProfile(Environment environment)
    {
        String profile = environment.getProperty(PROPERTY_PROFILE, PROFILE_MEMORY);
        
        return new OrientDbProfile(profile);
    }

    public static void bootstrap()
        throws IOException
    {
        String profile = System.getProperty(PROPERTY_PROFILE, PROFILE_MEMORY);
        
        logger.info("Bootstrap OrientDB connection properties with profile [{}].", profile);

        try (InputStream is = OrientDbProfile.class.getResourceAsStream(String.format("/META-INF/properties/%s.properties",
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

                          logger.debug("Using default value for OrientDB connection property [{}].", p);

                          System.setProperty((String) p, properties.getProperty((String) p));
                      });
        }

    }

}
