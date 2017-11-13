package com.orientechnologies.orient.object.jpa;

import org.carlspring.strongbox.data.domain.MyOObjectSerializer;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;
import java.util.logging.Logger;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.object.serialization.OObjectSerializerContext;
import com.orientechnologies.orient.object.serialization.OObjectSerializerHelper;

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

        logger.fine("EntityManagerFactory created. " + toString());
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
        Boolean automaticSchemaGeneration = Boolean.valueOf(properties.get(OJPAObjectDatabaseTxPersistence.PROPERTY_AUTOMATIC_SCHEMA_GENERATION)
                                                                      .toString());
        db.setAutomaticSchemaGeneration(Boolean.TRUE.equals(automaticSchemaGeneration));
        registerCustomTypes(db);
        return new OJPAObjectDatabaseTxEntityManager(db, this, properties);
    }

    private void registerCustomTypes(ODatabase db)
    {
        OObjectSerializerContext serializerContext = new OObjectSerializerContext();
        serializerContext.bind(MyOObjectSerializer.INSTANCE, db);
        OObjectSerializerHelper.bindSerializerContext(null, serializerContext);
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
