package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Przemyslaw Fusik
 */
public abstract class SingletonEntityProvider<T extends GenericEntity, ID extends Serializable>
{

    public abstract SingletonCrudService<T, ID> getService();

    public void save(T entityInstance)
    {
        if (entityInstance.getUuid() == null)
        {
            getService().deleteAll();
        }
        else
        {
            final Optional<T> maybeDbEntityInstance = getService().findOne();
            if (maybeDbEntityInstance.isPresent() &&
                !Objects.equals(entityInstance.getUuid(), maybeDbEntityInstance.get().getUuid()))
            {
                throw new DataServiceException("Only one entity instance is allowed");
            }
        }

        preSave(entityInstance);
        getService().save(entityInstance);
        postSave(entityInstance);
    }

    protected void postSave(final T entityInstance)
    {
        // override if needed
    }

    protected void preSave(final T entityInstance)
    {
        // override if needed
    }

    public Optional<T> get()
    {
        final Optional<T> maybeDbEntityInstance = getService().findOne();
        if (!maybeDbEntityInstance.isPresent())
        {
            return Optional.empty();
        }

        final T entityInstance = maybeDbEntityInstance.get();
        postGet(entityInstance);
        return Optional.of(entityInstance);
    }

    protected void postGet(final T entityInstance)
    {
        // override if needed
    }

}
