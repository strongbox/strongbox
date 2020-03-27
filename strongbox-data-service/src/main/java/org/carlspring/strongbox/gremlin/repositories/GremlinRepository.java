package org.carlspring.strongbox.gremlin.repositories;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

/**
 * @author sbespalov
 *
 */
public abstract class GremlinRepository<S extends Element, E extends DomainObject> implements CrudRepository<E, String>
{

    private static final Logger logger = LoggerFactory.getLogger(GremlinRepository.class);

    @Inject
    @TransactionContext
    private Graph graph;
    @Inject
    protected Session session;
    
    protected EntityTraversalSource g()
    {
        return graph.traversal(EntityTraversalSource.class);
    }

    protected abstract EntityTraversal<S, S> start(Supplier<EntityTraversalSource> g);

    public Optional<E> findById(String uuid)
    {
        Set<String> labels = adapter().labels();
        EntityTraversal<S, E> traversal = start(this::g).findById(uuid, labels.toArray(new String[labels.size()]))
                                                        .map(adapter().fold());
        if (!traversal.hasNext())
        {
            return Optional.empty();
        }

        return Optional.of(traversal.next());
    }

    @Override
    public <S extends E> Iterable<S> saveAll(Iterable<S> entities)
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    @Override
    public boolean existsById(String id)
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    @Override
    public Iterable<E> findAll()
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    @Override
    public Iterable<E> findAllById(Iterable<String> ids)
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    @Override
    public long count()
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    @Override
    public void deleteById(String id)
    {
        Set<String> labels = adapter().labels();
        start(this::g).findById(id, labels.toArray(new String[labels.size()]))
                      .flatMap(adapter().cascade())
                      .dedup()
                      .trace("Delete")
                      .drop()
                      .iterate();
        session.clear();
    }

    @Override
    public void delete(E entity)
    {
        deleteById(entity.getUuid());
    }

    @Override
    public void deleteAll(Iterable<? extends E> entities)
    {
        for (E entity : entities)
        {
            delete(entity);
        }
    }

    @Override
    public void deleteAll()
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    protected abstract EntityTraversalAdapter<S, E> adapter();
}
