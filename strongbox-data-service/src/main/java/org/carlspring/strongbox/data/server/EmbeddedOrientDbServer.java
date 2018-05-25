package org.carlspring.strongbox.data.server;

import org.carlspring.strongbox.config.ConnectionConfig;
import org.carlspring.strongbox.config.ConnectionConfigOrientDB;
import org.carlspring.strongbox.data.domain.GenericEntityHook;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.orientechnologies.orient.graph.server.command.OServerCommandGetGephi;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerCommandConfiguration;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerHookConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import com.orientechnologies.orient.server.network.protocol.http.command.get.OServerCommandGetStaticContent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.carlspring.strongbox.data.PropertyUtils.getVaultDirectory;

/**
 * An embedded configuration of OrientDb server.
 *
 * @author Alex Oreshkevich
 */
public class EmbeddedOrientDbServer
        implements OrientDbServer
{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedOrientDbServer.class);

    private OServer server;

    private OServerConfiguration serverConfiguration;

    @Inject
    private ConnectionConfig connectionConfig;

    @PostConstruct
    public void start()
    {
        try
        {
            prepareStudio();
            init();
            activate();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start the embedded OrientDb server!", e);
        }
    }

    public File getStudioClasspathLocation()
    {
        return new File(OServer.class.getProtectionDomain()
                                     .getCodeSource()
                                     .getLocation()
                                     .getPath()).toPath().resolveSibling("orientdb-studio-2.2.0.jar").toFile();
    }
    
    private void prepareStudio() throws IOException
    {
        String studioEnabled = System.getProperty(ConnectionConfigOrientDB.PROPERTY_STUDIO_ENABLED);
        if (studioEnabled != null && Boolean.FALSE.equals(studioEnabled))
        {
            logger.info(String.format("OrientDB Studio disabled with [%s], skip initialization.",
                                      ConnectionConfigOrientDB.PROPERTY_STUDIO_ENABLED));
            
            return;
        }
        
        File studioClasspathLocation = getStudioClasspathLocation();

        Path studioPath = Paths.get(getStudioPath()).resolve("studio");
        if (Files.exists(studioPath))
        {
            logger.info(String.format("OrientDB Studio already available at [%s], skip initialization.\\If you want to force initialize Studio please remove its folder above.",
                                      studioPath.toAbsolutePath().toString()));
            return;
        }

        logger.info(String.format("Initialize OrientDB Studio at [%s].", studioPath.toAbsolutePath().toString()));
        Files.createDirectories(studioPath);
        
        try (JarFile jar = new JarFile(studioClasspathLocation))
        {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements())
            {
                JarEntry file = enumEntries.nextElement();
                if (!file.getName().startsWith("META-INF/resources/webjars/orientdb-studio/2.2.0/"))
                {
                    continue;
                }
                
                File f = studioPath.resolve(file.getName().replace("META-INF/resources/webjars/orientdb-studio/2.2.0/",
                                                                   ""))
                                   .toFile();
                if (file.isDirectory() && !f.mkdir())
                {
                    logger.error(String.format("Failed to initialize OrientDB Studio at [%s]",
                                               studioPath.toAbsolutePath().toString()));
                    return;
                }
                try (InputStream is = jar.getInputStream(file))
                {
                    try (FileOutputStream fos = new java.io.FileOutputStream(f))
                    {
                        while (is.available() > 0)
                        {
                            fos.write(is.read());
                        }
                    }
                }
            }
        }
    }

    private void init()
        throws Exception
    {
        String database = connectionConfig.getDatabase();

        logger.info(String.format("Initialize Embedded OrientDB server for [%s]", database));

        server = OServerMain.create();
        serverConfiguration = new OServerConfiguration();

        OServerHookConfiguration hookConfiguration = new OServerHookConfiguration();
        serverConfiguration.hooks = Arrays.asList(new OServerHookConfiguration[] { hookConfiguration });
        hookConfiguration.clazz = GenericEntityHook.class.getName();

        OServerNetworkListenerConfiguration httpListener = new OServerNetworkListenerConfiguration();
        httpListener.ipAddress = connectionConfig.getHost();
        httpListener.portRange = "2480";
        httpListener.protocol = "http";
        httpListener.socket = "default";

        OServerCommandConfiguration httpCommandConfiguration1 = new OServerCommandConfiguration();
        httpCommandConfiguration1.implementation = OServerCommandGetStaticContent.class.getCanonicalName();
        httpCommandConfiguration1.pattern = "GET|www GET|studio/ GET| GET|*.htm GET|*.html GET|*.xml GET|*.jpeg GET|*.jpg GET|*.png GET|*.gif GET|*.js GET|*.css GET|*.swf GET|*.ico GET|*.txt GET|*.otf GET|*.pjs GET|*.svg GET|*.json GET|*.woff GET|*.ttf GET|*.svgz";
        httpCommandConfiguration1.stateful = false;
        httpCommandConfiguration1.parameters = new OServerEntryConfiguration[] { new OServerEntryConfiguration(
                "http.cache:*.htm *.html",
                "Cache-Control: no-cache, no-store, max-age=0, must-revalidate\\r\\nPragma: no-cache"),
                                                                                 new OServerEntryConfiguration(
                                                                                         "http.cache:default",
                                                                                         "Cache-Control: max-age=120") };

        OServerCommandConfiguration httpCommandConfiguration2 = new OServerCommandConfiguration();
        httpCommandConfiguration2.implementation = OServerCommandGetGephi.class.getCanonicalName();

        httpListener.commands = new OServerCommandConfiguration[] { httpCommandConfiguration1,
                                                                    httpCommandConfiguration2 };
        httpListener.parameters = new OServerParameterConfiguration[] { new OServerParameterConfiguration("utf-8",
                "network.http.charset") };

        OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
        binaryListener.ipAddress = connectionConfig.getHost();
        binaryListener.portRange = connectionConfig.getPort().toString();
        binaryListener.protocol = "binary";
        binaryListener.socket = "default";

        OServerNetworkProtocolConfiguration binaryProtocol = new OServerNetworkProtocolConfiguration();
        binaryProtocol.name = "binary";
        binaryProtocol.implementation = "com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary";

        OServerNetworkProtocolConfiguration httpProtocol = new OServerNetworkProtocolConfiguration();
        httpProtocol.name = "http";
        httpProtocol.implementation = "com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpDb";

        // prepare network configuration
        OServerNetworkConfiguration networkConfiguration = new OServerNetworkConfiguration();

        networkConfiguration.protocols = new LinkedList<>();
        networkConfiguration.protocols.add(binaryProtocol);
        networkConfiguration.protocols.add(httpProtocol);

        networkConfiguration.listeners = new LinkedList<>();
        networkConfiguration.listeners.add(binaryListener);
        networkConfiguration.listeners.add(httpListener);

        // add users (incl system-level root user)
        List<OServerUserConfiguration> users = new LinkedList<>();
        users.add(buildUser(connectionConfig.getUsername(), connectionConfig.getPassword(), "*"));

        System.setProperty("ORIENTDB_ROOT_PASSWORD", connectionConfig.getUsername());

        // add other properties
        List<OServerEntryConfiguration> properties = new LinkedList<>();
        properties.add(buildProperty("server.database.path", getDatabasePath()));
        properties.add(buildProperty("plugin.dynamic", "false"));
        properties.add(buildProperty("log.console.level", "linfo"));
        //properties.add(buildProperty("log.file.level", "fine"));
        properties.add(buildProperty("orientdb.www.path", getStudioPath()));

        serverConfiguration.network = networkConfiguration;
        serverConfiguration.users = users.toArray(new OServerUserConfiguration[users.size()]);
        serverConfiguration.properties = properties.toArray(new OServerEntryConfiguration[properties.size()]);
    }

    private void activate()
            throws Exception
    {
        if (!server.isActive())
        {
            server.startup(serverConfiguration);
            server.activate();
        }
    }

    private OServerUserConfiguration buildUser(String name,
                                               String password,
                                               String resources)
    {
        OServerUserConfiguration user = new OServerUserConfiguration();
        user.name = name;
        user.password = password;
        user.resources = resources;

        return user;
    }

    private OServerEntryConfiguration buildProperty(String name,
                                                    String value)
    {
        OServerEntryConfiguration property = new OServerEntryConfiguration();
        property.name = name;
        property.value = value;

        return property;
    }

    private String getDatabasePath()
    {
        return getVaultDirectory() + "/db";
    }

    private String getStudioPath()
    {
        return getVaultDirectory() + "/www";
    }

    
    @PreDestroy
    @Override
    public void stop()
    {
        server.shutdown();
    }

}
