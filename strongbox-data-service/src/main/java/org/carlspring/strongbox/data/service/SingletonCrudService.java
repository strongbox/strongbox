package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.data.domain.GenericEntity;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Przemyslaw Fusik
 */
public interface SingletonCrudService<T extends GenericEntity, ID extends Serializable>
        extends CrudService<T, ID>
{

    Optional<T> findOne();
}
