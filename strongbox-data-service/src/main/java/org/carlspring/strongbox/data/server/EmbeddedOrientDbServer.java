package org.carlspring.strongbox.data.server;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.data.domain.GenericEntityHook;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerHookConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.carlspring.strongbox.data.PropertyUtils.getVaultDirectory;

/**
 * An embedded configuration of OrientDb server.
 *
 * @author Alex Oreshkevich
 */
public class EmbeddedOrientDbServer
{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedOrientDbServer.class);

    private final DataServiceConfig dataServiceConfig;

    private OServer server;

    private OServerConfiguration serverConfiguration;

    public EmbeddedOrientDbServer(DataServiceConfig dataServiceConfig)
    {
        this.dataServiceConfig = dataServiceConfig;
    }

    public void init()
            throws Exception
    {
        if (server != null)
        {
            return;
        }

        server = OServerMain.create(false);
        serverConfiguration = new OServerConfiguration();
        
        OServerHookConfiguration hookConfiguration = new OServerHookConfiguration();
        serverConfiguration.hooks = Arrays.asList(new OServerHookConfiguration[] { hookConfiguration });
        hookConfiguration.clazz = GenericEntityHook.class.getName();

        OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
        binaryListener.ipAddress = dataServiceConfig.getHost();
        binaryListener.portRange = dataServiceConfig.getPort().toString();
        binaryListener.protocol = "binary";
        binaryListener.socket = "default";

        OServerNetworkProtocolConfiguration binaryProtocol = new OServerNetworkProtocolConfiguration();
        binaryProtocol.name = "binary";
        binaryProtocol.implementation = "com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary";

        // prepare network configuration
        OServerNetworkConfiguration networkConfiguration = new OServerNetworkConfiguration();
        networkConfiguration.protocols = new LinkedList<>();
        networkConfiguration.protocols.add(binaryProtocol);
        networkConfiguration.listeners = new LinkedList<>();
        networkConfiguration.listeners.add(binaryListener);

        // add users (incl system-level root user)
        List<OServerUserConfiguration> users = new LinkedList<>();
        users.add(buildUser(dataServiceConfig.getUsername(), dataServiceConfig.getPassword(), "*"));
        System.setProperty("ORIENTDB_ROOT_PASSWORD", dataServiceConfig.getUsername());

        // add other properties
        List<OServerEntryConfiguration> properties = new LinkedList<>();
        properties.add(buildProperty("server.database.path", getDatabasePath()));
        properties.add(buildProperty("plugin.dynamic", "false"));
        properties.add(buildProperty("log.console.level", "info"));
        properties.add(buildProperty("log.file.level", "fine"));

        serverConfiguration.network = networkConfiguration;
        serverConfiguration.users = users.toArray(new OServerUserConfiguration[users.size()]);
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
        return getVaultDirectory() + "/db";
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
            throw new RuntimeException("Unable to start the embedded OrientDb server!", e);
        }
    }
}
