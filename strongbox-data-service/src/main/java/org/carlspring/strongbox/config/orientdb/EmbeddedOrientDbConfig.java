package org.carlspring.strongbox.config.orientdb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.strongbox.db.server.EmbeddedOrientDbServer;
import org.strongbox.db.server.OrientDbServer;
import org.strongbox.db.server.OrientDbServerConfiguration;
import org.strongbox.db.server.OrientDbStudioConfiguration;

import com.orientechnologies.orient.core.db.OrientDB;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Conditional(EmbeddedOrientDbConfig.class)
public class EmbeddedOrientDbConfig
        extends CommonOrientDbConfig
        implements Condition
{

    private static final String PATH_STRONGBOX_DB = "META-INF/resources/strongbox/db";

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedOrientDbConfig.class);


    @Bean(destroyMethod = "close")
    @DependsOn("orientDbServer")
    OrientDB orientDB(OrientDbServerConfiguration serverProperties)
            throws IOException, URISyntaxException
    {
        String url = StringUtils.substringBeforeLast(serverProperties.getUrl(), "/");
        OrientDB orientDB = new OrientDB(url,
                                         serverProperties.getUsername(),
                                         serverProperties.getPassword(),
                                         getOrientDBConfig());
        String database = serverProperties.getDatabase();
        if (orientDB.exists(database))
        {
            logger.info("Re-using existing database {}.", database);
            
            return orientDB;
        }
        logger.info("Database does not exist. Copying fresh database snapshot from classpath...");

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

                Path filePath = Paths.get(serverProperties.getPath(), file.getName().replace(PATH_STRONGBOX_DB, ""));
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
        
        return orientDB;
    }

    public JarFile getDbSchemaClasspathLocation()
        throws IOException
    {
        URL systemResource = EmbeddedOrientDbServer.class.getResource(String.format("/%s", PATH_STRONGBOX_DB));
        JarURLConnection connection = (JarURLConnection) systemResource.openConnection();

        return connection.getJarFile();
    }
    
    @Bean
    OrientDbServer orientDbServer(OrientDbServerConfiguration serverProperties, OrientDbStudioConfiguration studioProperties) 
    {
        return new EmbeddedOrientDbServer(studioProperties, serverProperties);
    }
    
    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        OrientDbProfile profile = OrientDbProfile.resolveProfile(conditionContext.getEnvironment());
        
        return profile.getName().equals(OrientDbProfile.PROFILE_EMBEDDED);
    }
}
