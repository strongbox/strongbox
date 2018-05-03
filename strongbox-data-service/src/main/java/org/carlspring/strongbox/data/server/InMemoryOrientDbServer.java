package org.carlspring.strongbox.data.server;

import org.carlspring.strongbox.config.ConnectionConfig;
import org.carlspring.strongbox.config.ConnectionConfigOrientDB;
import org.carlspring.strongbox.data.domain.GenericEntityHook;

import javax.inject.Inject;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseLifecycleListener;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

@Component("orientDbServer")
@Lazy(false)
@Conditional(InMemoryOrientDbServer.class)
public class InMemoryOrientDbServer
        implements OrientDbServer, Condition, ODatabaseLifecycleListener
{

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrientDbServer.class);

    private static final String ORIENTDB_DEFAULT_USERNAME = "admin";

    private static final String ORIENTDB_DEFAULT_PASSWORD = "admin";

    private OrientDB orientDB;

    @Inject
    private ConnectionConfig connectionConfig;

    private ORecordHook genericEntityHook = new GenericEntityHook();

    @Override
    public void start()
    {
        String database = connectionConfig.getDatabase();
        logger.info(String.format("Initialize In-Memory OrientDB server for [%s]", database));

        Orient.instance().addDbLifecycleListener(this);

        orientDB = new OrientDB(connectionConfig.getUrl(), connectionConfig.getUsername(),
                                connectionConfig.getPassword(), null);
        if (!orientDB.exists(database))
        {
            logger.info(String.format("Creating database [%s]...", database));
            orientDB.create(database, ODatabaseType.MEMORY);

            try (ODatabaseSession session = orientDB.open(database, ORIENTDB_DEFAULT_USERNAME,
                                                          ORIENTDB_DEFAULT_PASSWORD))
            {
                session.command("UPDATE ouser SET password = :password WHERE name = :name",
                                new Object[]{ connectionConfig.getPassword(),
                                              connectionConfig.getUsername() });
                session.commit();
            }
        }
    }

    public OrientDB orientDB()
    {
        return orientDB;
    }

    @Override
    public void stop()
    {
        orientDB.close();
    }

    @Override
    public PRIORITY getPriority()
    {
        return PRIORITY.FIRST;
    }

    @Override
    public void onCreate(ODatabaseInternal iDatabase)
    {
        iDatabase.registerHook(genericEntityHook);
    }

    @Override
    public void onOpen(ODatabaseInternal iDatabase)
    {
        iDatabase.registerHook(genericEntityHook);
    }

    @Override
    public void onClose(ODatabaseInternal iDatabase)
    {
        iDatabase.registerHook(genericEntityHook);
    }

    @Override
    public void onDrop(ODatabaseInternal iDatabase)
    {
        iDatabase.registerHook(genericEntityHook);
    }

    @Override
    public void onCreateClass(ODatabaseInternal iDatabase,
                              OClass iClass)
    {
        iDatabase.registerHook(genericEntityHook);
    }

    @Override
    public void onDropClass(ODatabaseInternal iDatabase,
                            OClass iClass)
    {
        iDatabase.registerHook(genericEntityHook);
    }

    @Override
    public void onLocalNodeConfigurationRequest(ODocument iConfiguration)
    {
    }

    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        return ConnectionConfigOrientDB.resolveProfile(conditionContext.getEnvironment())
                                       .equals(ConnectionConfigOrientDB.PROFILE_MEMORY);
    }

}
