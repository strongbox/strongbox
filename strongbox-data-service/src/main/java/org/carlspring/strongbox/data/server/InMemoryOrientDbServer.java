package org.carlspring.strongbox.data.server;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.carlspring.strongbox.data.domain.GenericEntityHook;
import org.strongbox.db.server.OrientDbServer;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseLifecycleListener;
import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class InMemoryOrientDbServer
        implements OrientDbServer, ODatabaseLifecycleListener
{

    private ORecordHook genericEntityHook = new GenericEntityHook();

    @PostConstruct
    @Override
    public void start()
    {
        Orient.instance().addDbLifecycleListener(this);
    }

    @PreDestroy
    @Override
    public void stop()
    {
        // do nothing
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
    public void onLocalNodeConfigurationRequest(ODocument iConfiguration)
    {
    }


}
