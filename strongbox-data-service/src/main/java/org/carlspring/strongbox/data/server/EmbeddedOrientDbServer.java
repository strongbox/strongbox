package org.carlspring.strongbox.data.server;

import static org.carlspring.strongbox.data.PropertyUtils.getVaultDirectory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.carlspring.strongbox.config.ConnectionConfig;
import org.carlspring.strongbox.config.ConnectionConfigOrientDB;
import org.carlspring.strongbox.data.domain.GenericEntityHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.config.OServerHookConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkListenerConfiguration;
import com.orientechnologies.orient.server.config.OServerNetworkProtocolConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;

/**
 * An embedded configuration of OrientDb server.
 *
 * @author Alex Oreshkevich
 */
@Component("orientDbServer")
@Lazy(false)
@Conditional(EmbeddedOrientDbServer.class)
public class EmbeddedOrientDbServer implements OrientDbServer, Condition
{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedOrientDbServer.class);

    private OServer server;

    private OServerConfiguration serverConfiguration;

    @Inject
    private ConnectionConfig connectionConfig;

    public void init()
        throws Exception
    {
        String database = connectionConfig.getDatabase();
        logger.info(String.format("Initialize Embedded OrientDB server for [%s]", database));

        server = OServerMain.create();
        serverConfiguration = new OServerConfiguration();

        OServerHookConfiguration hookConfiguration = new OServerHookConfiguration();
        serverConfiguration.hooks = Arrays.asList(new OServerHookConfiguration[] { hookConfiguration });
        hookConfiguration.clazz = GenericEntityHook.class.getName();

        OServerNetworkListenerConfiguration binaryListener = new OServerNetworkListenerConfiguration();
        binaryListener.ipAddress = connectionConfig.getHost();
        binaryListener.portRange = connectionConfig.getPort().toString();
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
        users.add(buildUser(connectionConfig.getUsername(), connectionConfig.getPassword(), "*"));
        System.setProperty("ORIENTDB_ROOT_PASSWORD", connectionConfig.getUsername());

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

    @Override
    @PreDestroy
    public void stop()
    {
        server.shutdown();
    }

    @PostConstruct
    public void start()
    {
        
        try
        {
            init();
            activate();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to start the embedded OrientDb server!", e);
        }

    }

    private void activate()
        throws Exception
    {
        if (!server.isActive())
        {
            server.startup(serverConfiguration);
            server.activate();
        }

        OServerAdmin serverAdmin = new OServerAdmin(connectionConfig.getUrl()).connect(connectionConfig.getUsername(),
                                                                                       connectionConfig.getPassword());
        if (!serverAdmin.existsDatabase())
        {
            logger.info(String.format("Creating database [%s]...", connectionConfig.getDatabase()));

            serverAdmin.createDatabase(connectionConfig.getDatabase(), "document", "plocal");
        }
        else
        {
            logger.info("Reuse existing database " + connectionConfig.getDatabase());
        }
    }

    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        return ConnectionConfigOrientDB.resolveProtocol(conditionContext.getEnvironment()).equals("remote");
    }

}
