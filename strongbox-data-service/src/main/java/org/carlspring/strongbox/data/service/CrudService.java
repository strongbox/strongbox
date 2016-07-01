package org.carlspring.strongbox.data.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

/**
 * Copy of {@link org.springframework.data.repository.CrudRepository} from API functions set point of view.
 *
 * @author Alex Oreshkevich
 */
@Transactional
public interface CrudService<T, ID extends Serializable>
{

    <S extends T> S save(S var1);

    <S extends T> Iterable<S> save(Iterable<S> var1);

    Optional<T> findOne(ID var1);

    boolean exists(ID var1);

    Optional<List<T>> findAll();

    Optional<List<T>> findAll(List<ID> var1);

    long count();

    void delete(ID var1);

    void delete(T var1);

    void delete(Iterable<? extends T> var1);

    void deleteAll();

}
