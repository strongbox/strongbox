package org.carlspring.strongbox.data.tx;

import com.orientechnologies.orient.object.db.OObjectDatabasePool;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;

/**
 * @author Alex Oreshkevich
 */
public class CustomOrientObjectDatabaseFactory extends OrientObjectDatabaseFactory
{
    
    private OObjectDatabasePool pool;
    private OObjectDatabaseTx db;

    public CustomOrientObjectDatabaseFactory()
    {
    }

    protected void createPool()
    {
        this.pool = new OObjectDatabasePool(this.getUrl(), this.getUsername(), this.getPassword());
        this.pool.setup(this.minPoolSize, this.maxPoolSize);
    }

    public OObjectDatabaseTx openDatabase()
    {
        this.db = this.pool.acquire();
        return this.db;
    }

    public OObjectDatabaseTx db()
    {
        return super.db();
    }

    protected OObjectDatabaseTx newDatabase()
    {
        return new OObjectDatabaseTx(this.getUrl());
    }

    public void close()
    {
        pool.close();
    }
    
}
