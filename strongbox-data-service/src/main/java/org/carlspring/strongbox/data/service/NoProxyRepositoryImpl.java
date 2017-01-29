package org.carlspring.strongbox.data.service;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.orient.commons.repository.support.SimpleOrientRepository;

/**
 * @param <T>
 * @author Alex Oreshkevich
 */
public class NoProxyRepositoryImpl<T>
        extends SimpleOrientRepository<T>
{

    private OObjectDatabaseTx databaseTx;

    public NoProxyRepositoryImpl(OrientOperations operations,
                                 Class<T> domainClass,
                                 Class<?> repositoryInterface)
    {
        super(operations, domainClass, repositoryInterface);
    }

    public NoProxyRepositoryImpl(OrientOperations operations,
                                 Class<T> domainClass,
                                 String cluster,
                                 Class<?> repositoryInterface)
    {
        super(operations, domainClass, cluster, repositoryInterface);
    }

    public void setDatabaseTx(OObjectDatabaseTx databaseTx)
    {
        this.databaseTx = databaseTx;
    }

    @Override
    public <S extends T> S save(S entity)
    {
        databaseTx.activateOnCurrentThread();
        return databaseTx.detachAll(super.save(entity), true);
    }

    @Override
    public T findOne(String id)
    {
        databaseTx.activateOnCurrentThread();
        return databaseTx.detachAll(super.findOne(id), true);
    }
}
