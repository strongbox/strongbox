package org.carlspring.strongbox.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

public class ConnectionConfigOrientDB implements ConnectionConfig
{

    private static final Logger logger = LoggerFactory.getLogger(ConnectionConfigOrientDB.class);

    public static final String PROPERTY_PROFILE = "strongbox.orientdb.profile";

    public static final String PROFILE_MEMORY = "orientdb_MEMORY";
    public static final String PROFILE_EMBEDDED = "orientdb_EMBEDDED";
    public static final String PROFILE_REMOTE = "orientdb_REMOTE";

    @Value("${strongbox.orientdb.protocol:}")
    private String protocol;

    @Value("${strongbox.orientdb.host:}")
    private String host;

    @Value("${strongbox.orientdb.port:}")
    private Integer port;

    @Value("${strongbox.orientdb.database:}")
    private String database;

    @Value("${strongbox.orientdb.username:}")
    private String username;

    @Value("${strongbox.orientdb.password:}")
    private String password;

    public String getUrl()
    {
        if ("memory".equals(protocol))
        {
            return String.format("%s:%s", protocol, database);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(protocol);
        if (host != null)
        {
            sb.append(":").append(host);
        }
        if (port != null)
        {
            sb.append(":").append(port);
        }
        sb.append("/").append(database);

        return sb.toString();
    }

    public String getPropertyProtocol()
    {
        return protocol;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public static String resolveProfile(Environment environment)
    {
        return environment.getProperty(PROPERTY_PROFILE, PROFILE_MEMORY);
    }

    public static void bootstrap()
        throws IOException
    {
        String profile = System.getProperty(PROPERTY_PROFILE, PROFILE_MEMORY);
        logger.info(String.format("Bootstrap OrientDB connection properties with profile [%s].",
                                  profile));

        try (InputStream is = ConnectionConfigOrientDB.class
                                                      .getResourceAsStream(String.format("/META-INF/properties/%s.properties", profile)))
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

                          logger.info(String.format("Use default value for OrientDB connection property [%s].", p));

                          System.setProperty((String) p, properties.getProperty((String) p));
                      });
        }

    }

}
