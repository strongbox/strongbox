package org.carlspring.strongbox.data.service;

import java.util.LinkedList;
import java.util.List;

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

    @Override
    public List<T> findAll()
    {
        List<T> result = super.findAll();
        if (result != null && !result.isEmpty())
        {

            // TODO Due to internal error in spring-data-orientdb
            // com.orientechnologies.orient.client.remote.OStorageRemote cannot be cast to
            // com.orientechnologies.orient.core.storage.impl.local.paginated.OLocalPaginatedStorage
            // we have to do manual detach for all list entries
            databaseTx.activateOnCurrentThread();
            final int size = result.size();
            List<T> obtainedResult = new LinkedList<>();
            for (int i = 0; i < size; i++)
            {
                obtainedResult.add(databaseTx.detachAll(result.get(i), true));
            }
            return obtainedResult;
        }
        else
        {
            return result;
        }
    }
}
