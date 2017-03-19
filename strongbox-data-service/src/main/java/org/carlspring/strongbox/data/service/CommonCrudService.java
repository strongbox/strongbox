package org.carlspring.strongbox.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.springframework.transaction.annotation.Transactional;

import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@Transactional
public abstract class CommonCrudService<T extends GenericEntity> implements CrudService<T, String>
{
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <S extends T> S save(S entity)
    {
        return getDelegate().save(entity);
    }

    @Override
    public Optional<T> findOne(String id)
    {
        if (id == null)
        {
            return Optional.empty();
        }

        return Optional.ofNullable((T) entityManager.find(getEntityClass(), id));
    }

    @Override
    public boolean exists(String id)
    {
        return findOne(id) != null;
    }

    @Override
    public Optional<List<T>> findAll()
    {
        List<T> resultList = new ArrayList<>();
        for (T t : getDelegate().browseClass(getEntityClass()))
        {
            resultList.add(t);
        }
        return Optional.of(resultList);
    }

    @Override
    public long count()
    {
        return getDelegate().countClass(getEntityClass());
    }

    @Override
    public void delete(String id)
    {
        getDelegate().delete(new ORecordId(id));
    }

    @Override
    public void delete(T entity)
    {
        entityManager.remove(entity);
    }

    @Override
    public void deleteAll()
    {
        Optional<List<T>> findAll = findAll();
        if (!findAll.isPresent())
        {
            return;
        }
        for (T entity : findAll.get())
        {
            delete(entity);
        }
    }

    /**
     * We can get an internal OrientDB transaction API with this, which can be needed to execute some OrientDB queries,
     * for example.
     * 
     * @return
     */
    protected OObjectDatabaseTx getDelegate()
    {
        return (OObjectDatabaseTx) entityManager.getDelegate();
    }

    public abstract Class<T> getEntityClass();
}
