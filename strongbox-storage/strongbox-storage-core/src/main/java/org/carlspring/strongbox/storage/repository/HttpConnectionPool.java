package org.carlspring.strongbox.storage.repository;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class HttpConnectionPool
{

    private final int allocatedConnections;


    public HttpConnectionPool(final MutableHttpConnectionPool delegate)
    {
        allocatedConnections = delegate.getAllocatedConnections();
    }

    public int getAllocatedConnections()
    {
        return allocatedConnections;
    }
}
