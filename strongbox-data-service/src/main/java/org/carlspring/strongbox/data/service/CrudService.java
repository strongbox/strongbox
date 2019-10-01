package org.carlspring.strongbox.data.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * Copy of {@link org.springframework.data.repository.CrudRepository} from API functions set point of view.
 *
 * @author Alex Oreshkevich
 */
public interface CrudService<T extends GenericEntity, ID extends Serializable>
{

    <S extends T> S save(S entity);

    T lockOne(ID id);

    Optional<T> findOne(ID id);

    boolean existsByUuid(String uuid);
    
    boolean exists(ID id);

    Optional<List<T>> findAll();

    long count();

    void delete(ID id);

    void delete(T entity);
    
    int delete(List<T> entityList);

    void deleteAll();

    Class<T> getEntityClass();

}
