package com.orientechnologies.orient.object.jpa;

import javax.persistence.spi.PersistenceProvider;

import com.orientechnologies.orient.core.db.ODatabasePool;
import org.springframework.orm.jpa.JpaVendorAdapter;

/**
 * @author Przemyslaw Fusik
 */
public class OrientDbJpaVendorAdapter implements JpaVendorAdapter
{


    private final ODatabasePool pool;

    public OrientDbJpaVendorAdapter(final ODatabasePool pool)
    {
        this.pool = pool;
    }

    @Override
    public PersistenceProvider getPersistenceProvider()
    {
        return new OJPAObjectDatabaseTxPersistence(pool);
    }
}
