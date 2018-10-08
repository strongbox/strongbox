package com.orientechnologies.orient.object.jpa;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.jdbc.OrientDataSource;
import com.orientechnologies.orient.jdbc.OrientJdbcConnection;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * @author Sergey Bespalov
 * @author Przemyslaw Fusik
 *
 */
public class OJPAObjectDatabaseTxEntityManagerFactory
        implements EntityManagerFactory
{
    /** the log used by this class. */
    private static Logger logger = Logger.getLogger(OJPAObjectDatabaseTxEntityManagerFactory.class.getName());

    private final OrientDataSource dataSource;
    private final OJPAProperties properties;


    public OJPAObjectDatabaseTxEntityManagerFactory(final OJPAProperties properties,
                                                    final OrientDataSource dataSource)
    {
        this.properties = properties;
        this.dataSource = dataSource;

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

        Connection connection;
        try
        {
            connection = dataSource.getConnection();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        OObjectDatabaseTx db = new OObjectDatabaseTx((ODatabaseDocumentInternal) ((OrientJdbcConnection) connection).getDatabase());
        Boolean automaticSchemaGeneration = Boolean.valueOf(properties.getOrDefault(
                OJPAObjectDatabaseTxPersistenceProvider.PROPERTY_AUTOMATIC_SCHEMA_GENERATION,
                Boolean.FALSE.toString())
                                                                      .toString());
        db.setAutomaticSchemaGeneration(Boolean.TRUE.equals(automaticSchemaGeneration));
        return new OJPAObjectDatabaseTxEntityManager(db, this, properties);
    }

    @Override
    public void close()
    {
        dataSource.close();
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
