package com.orientechnologies.orient.object.jpa;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * @author Sergey Bespalov
 *
 */
public class OJPAPartitionedEntityManagerPool implements EntityManagerFactory
{
    /** the log used by this class. */
    private static Logger logger = Logger.getLogger(OJPAPartitionedEntityManagerPool.class.getName());

    private OPartitionedDatabasePool databasePool;
    private final OJPAProperties properties;

    public OJPAPartitionedEntityManagerPool(final OJPAProperties properties)
    {
        this.properties = properties;
        this.databasePool = new OPartitionedDatabasePool(properties.getURL(), properties.getUser(),
                properties.getPassword(), 100, 100);

        if (logger.isLoggable(Level.INFO))
        {
            logger.info("EntityManagerFactory created. " + toString());
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public EntityManager createEntityManager(final Map map)
    {
        return createEntityManager(new OJPAProperties(map));
    }

    @Override
    public EntityManager createEntityManager()
    {
        return createEntityManager(properties);
    }

    private EntityManager createEntityManager(final OJPAProperties properties)
    {
        OObjectDatabaseTx db = new OObjectDatabaseTx(databasePool.acquire());
        return new OJPAObjectDatabaseTxEntityManager(db, this, properties);
    }

    @Override
    public void close()
    {
        databasePool.close();
        if (logger.isLoggable(Level.INFO))
        {
            logger.info("EntityManagerFactory closed. " + toString());
        }
    }

    @Override
    public boolean isOpen()
    {
        return !databasePool.isClosed();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder()
    {
        throw new UnsupportedOperationException("getCriteriaBuilder");
    }

    @Override
    public Metamodel getMetamodel()
    {
        throw new UnsupportedOperationException("getMetamodel");
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return properties.getUnmodifiableProperties();
    }

    @Override
    public Cache getCache()
    {
        throw new UnsupportedOperationException("getCache");
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil()
    {
        throw new UnsupportedOperationException("getPersistenceUnitUtil");
    }
}
