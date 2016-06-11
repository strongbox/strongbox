package org.carlspring.strongbox.data.server;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * An embedded configuration of OrientDb server.
 *
 * @author Alex Oreshkevich
 */
@Component
public class EmbeddedOrientDbServer
{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedOrientDbServer.class);

    @Value("${org.carlspring.strongbox.data.orientdb.host}")
    String host;

    private OServer server;
    private OServerConfiguration serverConfiguration;

    @Value("${org.carlspring.strongbox.data.orientdb.user}")
    String user;

    @Value("${org.carlspring.strongbox.data.orientdb.password}")
    String password;


    @PostConstruct
    public void init()
            throws Exception
    {
        server = OServerMain.create();
        serverConfiguration = new OServerConfiguration();

        // prepare network configuration
        OServerNetworkConfiguration networkConfiguration = new OServerNetworkConfiguration();
        serverConfiguration.network = networkConfiguration;

        networkConfiguration.protocols = new LinkedList<>();

        OServerNetworkProtocolConfiguration binaryProtocol = new OServerNetworkProtocolConfiguration();
        binaryProtocol.name = "binary";
        binaryProtocol.implementation = "com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary";

        networkConfiguration.protocols.add(binaryProtocol);

        networkConfiguration.listeners = new LinkedList<>();

        OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
        binaryListener.ipAddress = "0.0.0.0";
        binaryListener.portRange = "2424-2430";
        binaryListener.protocol = "binary";
        binaryListener.socket = "default";
        networkConfiguration.listeners.add(binaryListener);

        // add users (incl system-level root user)
        List<OServerUserConfiguration> users = new LinkedList<>();
        users.add(buildUser(user, password, "*"));
        System.setProperty("ORIENTDB_ROOT_PASSWORD", user);
        serverConfiguration.users = users.toArray(new OServerUserConfiguration[users.size()]);

        // add other properties
        List<OServerEntryConfiguration> properties = new LinkedList<>();
        properties.add(buildProperty("server.database.path", getDatabasePath()));
        properties.add(buildProperty("plugin.dynamic", "false"));
        properties.add(buildProperty("log.console.level", "info"));
        properties.add(buildProperty("log.file.level", "fine"));
        serverConfiguration.properties = properties.toArray(new OServerEntryConfiguration[properties.size()]);
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
        String databasePath = System.getProperty("user.dir") + "/target/db";
        logger.debug("OrientDB database " + databasePath);
        // return ConfigurationResourceResolver.getVaultDirectory() + "/db";
        return databasePath;
    }

    public void start()
    {
        try
        {
            if (!server.isActive())
            {
                server.startup(serverConfiguration);
                server.activate();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start embedded OrientDb server", e);
        }
    }

    // actually there is no need for manual shutdown
    // it's executed as a part of build / execution of app server finalisation
    @SuppressWarnings("unused")
    public void shutDown()
    {
        if (server.isActive())
        {
            server.shutdown();
        }
    }
}
