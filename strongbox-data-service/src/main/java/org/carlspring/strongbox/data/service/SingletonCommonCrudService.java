package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author Przemyslaw Fusik
 */
public abstract class SingletonCommonCrudService<T extends GenericEntity>
        extends CommonCrudService<T>
        implements SingletonCrudService<T, String>

{

    @Override
    public Optional<T> findOne()
    {
        final Optional<List<T>> all = findAll();
        if (!all.isPresent())
        {
            return Optional.empty();
        }
        final List<T> instances = all.get();
        if (instances.size() != 1)
        {
            throw new IllegalStateException("Found more than one instance");
        }
        return Optional.of(instances.get(0));
    }

}
