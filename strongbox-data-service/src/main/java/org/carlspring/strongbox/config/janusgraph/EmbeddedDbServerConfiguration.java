package org.carlspring.strongbox.config.janusgraph;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.carlspring.strongbox.db.schema.StrongboxSchema;
import org.janusgraph.core.JanusGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.strongbox.db.server.CassandraEmbeddedConfiguration;
import org.strongbox.db.server.CassandraEmbeddedProperties;
import org.strongbox.db.server.JanusGraphConfiguration;
import org.strongbox.db.server.JanusGraphProperties;
import org.strongbox.db.server.JanusGraphServer;
import org.strongbox.db.server.JanusGraphWithEmbeddedCassandra;

/**
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@Configuration
@Conditional(EmbeddedDbServerConfiguration.class)
public class EmbeddedDbServerConfiguration implements Condition
{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedDbServerConfiguration.class);

    public static final String PATH_STRONGBOX_DB = "META-INF/org/carlsparing/strongbox/db";

    @Bean
    JanusGraphServer embeddedDbServer(CassandraEmbeddedConfiguration cassandraConfiguration,
                                      JanusGraphConfiguration janusGraphConfiguration)
    {

        if (!Files.exists(Paths.get(cassandraConfiguration.getStorageRoot())))
        {
            logger.info(String.format("Extract storage from [%s].", PATH_STRONGBOX_DB));
            initStorage(cassandraConfiguration);
            logger.info(String.format("Storage extracted to [%s].", cassandraConfiguration.getStorageRoot()));
        }

        return new JanusGraphWithEmbeddedCassandra(cassandraConfiguration, janusGraphConfiguration);
    }

    public JarFile getDbSchemaClasspathLocation()
        throws IOException
    {
        URL systemResource = JanusGraphWithEmbeddedCassandra.class.getResource("/" + PATH_STRONGBOX_DB);
        if (systemResource == null)
        {
            throw new IOException(String.format("Storage resource [%s] not found.", PATH_STRONGBOX_DB));
        }
        JarURLConnection connection = (JarURLConnection) systemResource.openConnection();

        return connection.getJarFile();
    }

    private void initStorage(CassandraEmbeddedConfiguration cassandraConfiguration)
    {
        try (JarFile jar = getDbSchemaClasspathLocation())
        {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements())
            {
                JarEntry file = enumEntries.nextElement();
                if (!file.getName().startsWith(PATH_STRONGBOX_DB))
                {
                    continue;
                }

                Path filePath = Paths.get(cassandraConfiguration.getStorageRoot(),
                                          file.getName().replace(PATH_STRONGBOX_DB, ""));
                if (file.isDirectory())
                {
                    Files.createDirectories(filePath);
                    continue;
                }

                try (InputStream is = new BufferedInputStream(jar.getInputStream(file)))
                {
                    try (OutputStream os = new BufferedOutputStream(new java.io.FileOutputStream(filePath.toFile())))
                    {
                        while (is.available() > 0)
                        {
                            os.write(is.read());
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            logger.warn(String.format("Failed to extract Strongbox storage resource from [%s], reason [%s].",
                                      PATH_STRONGBOX_DB, e.getMessage()));
        }
    }

    @Bean
    JanusGraph JanusGraph(JanusGraphServer server)
        throws Exception
    {
        return new StrongboxSchema().createSchema(server.getJanusGraph());
    }

    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        JanusGraphDbProfile profile = JanusGraphDbProfile.resolveProfile((ConfigurableEnvironment) conditionContext.getEnvironment());

        return profile.getName().equals(JanusGraphDbProfile.PROFILE_EMBEDDED);
    }

}
