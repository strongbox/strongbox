package com.orientechnologies.orient.object.jpa;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;
import java.util.logging.Logger;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabasePool;
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

    private final ODatabasePool databasePool;
    private final OJPAProperties properties;


    public OJPAPartitionedEntityManagerPool(final OJPAProperties properties,
                                            final ODatabasePool databasePool)
    {
        this.properties = properties;
        this.databasePool = databasePool;

        logger.fine("EntityManagerFactory created. " + toString());
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public EntityManager createEntityManager(final Map map)
    {
        return createEntityManager(new OJPAProperties(map));
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType)
    {
        throw new UnsupportedOperationException("createEntityManager");
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map)
    {
        throw new UnsupportedOperationException("createEntityManager");
    }

    @Override
    public EntityManager createEntityManager()
    {
        return createEntityManager(properties);
    }

    private EntityManager createEntityManager(final OJPAProperties properties)
    {
        OObjectDatabaseTx db = new OObjectDatabaseTx((ODatabaseDocumentInternal) databasePool.acquire());
        Boolean automaticSchemaGeneration = Boolean.valueOf(properties.getOrDefault(OJPAObjectDatabaseTxPersistence.PROPERTY_AUTOMATIC_SCHEMA_GENERATION,
                                                                                    Boolean.FALSE.toString())
                                                                      .toString());
        db.setAutomaticSchemaGeneration(Boolean.TRUE.equals(automaticSchemaGeneration));
        return new OJPAObjectDatabaseTxEntityManager(db, this, properties);
    }

    @Override
    public void close()
    {
        databasePool.close();
        logger.fine("EntityManagerFactory closed. " + toString());
    }

    @Override
    public boolean isOpen()
    {
        return true;
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

    @Override
    public void addNamedQuery(String s, Query query)
    {
        throw new UnsupportedOperationException("addNamedQuery");
    }

    @Override
    public <T> T unwrap(Class<T> aClass)
    {
        throw new UnsupportedOperationException("unwrap");
    }

    @Override
    public <T> void addNamedEntityGraph(String s, EntityGraph<T> entityGraph)
    {
        throw new UnsupportedOperationException("addNamedEntityGraph");
    }
}
