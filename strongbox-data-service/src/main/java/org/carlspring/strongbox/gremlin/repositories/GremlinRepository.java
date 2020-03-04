package org.carlspring.strongbox.gremlin.repositories;

import static org.reflections.ReflectionUtils.withAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.gremlin.tx.TransactionContext;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;

/**
 * @author sbespalov
 *
 */
public abstract class GremlinRepository<S, E extends DomainObject> implements CrudRepository<E, String>
{

    private static final Logger logger = LoggerFactory.getLogger(GremlinRepository.class);

    protected final Map<Class<? extends E>, String> labelsMap;

    @Inject
    @TransactionContext
    private Graph graph;

    public GremlinRepository(EntityType entityType,
                             Class<E> entityClass)
    {
        Class<? extends Annotation> annotation = entityType.getEntityTypeAnnotation();
        Method annotationValue = ReflectionUtils.getMethods(annotation, m -> "value".equals(m.getName()))
                                                .iterator()
                                                .next();
        Reflections reflections = new Reflections("org.carlspring.strongbox.artifact.coordinates",
                "org.carlspring.strongbox.domain");
        labelsMap = reflections.getSubTypesOf(entityClass)
                               .stream()
                               .filter(withAnnotation(annotation))
                               .collect(Collectors.toMap(c -> c,
                                                         c -> entityTypeLabel(c.getAnnotation(annotation),
                                                                              annotationValue)));
    }

    private static String entityTypeLabel(Annotation entityTypeAnnotation,
                                          Method valueMethod)
    {
        try
        {
            return (String) valueMethod.invoke(entityTypeAnnotation);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected String label(Class<?> entityClass)
    {
        return Optional.of(entityClass)
                       .map(labelsMap::get)
                       .orElseThrow(() -> new IllegalArgumentException(
                               String.format("Declared label not found for type [%s].", entityClass.getSimpleName())));
    }

    protected String[] labels()
    {
        return labelsMap.values().toArray(new String[labelsMap.size()]);
    }

    protected EntityTraversalSource g()
    {
        return graph.traversal(EntityTraversalSource.class);
    }

    protected abstract EntityTraversal<S, S> start(Supplier<EntityTraversalSource> g);

    public Optional<E> findById(String uuid)
    {
        EntityTraversal<S, E> traversal = start(this::g).findById(uuid, labels())
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
        start(this::g).findById(id, labels())
                      .flatMap(adapter().cascade())
                      .dedup()
                      .sideEffect(t -> logger.debug(String.format("Delete [%s]-[%s]", t.get().label(), t.get().id())))
                      .drop()
                      .iterate();
    }

    @Override
    public void delete(E entity)
    {
        deleteById(entity.getUuid());
    }

    @Override
    public void deleteAll(Iterable<? extends E> entities)
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    @Override
    public void deleteAll()
    {
        throw new UnsupportedOperationException("TODO implement");
    }

    protected abstract EntityTraversalAdapter<S, E> adapter();
}
