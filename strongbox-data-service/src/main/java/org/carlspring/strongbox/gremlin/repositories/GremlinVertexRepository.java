package org.carlspring.strongbox.gremlin.repositories;

import java.util.function.Supplier;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.data.domain.DomainObject;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;

public abstract class GremlinVertexRepository<E extends DomainObject> extends GremlinRepository<Vertex, E>
{

    public GremlinVertexRepository(Class<E> entityClass)
    {
        super(EntityType.VERTEX, entityClass);
    }

    @Override
    public <R extends E> R save(R entity)
    {
        Vertex resultVertex = start(this::g).saveV(label(entity.getClass()), entity.getUuid(), adapter().unfold(entity))
                                            .next();
        E resultEntity = findById(resultVertex.<String>property("uuid").value()).get();

        return (R) resultEntity;
    }

    @Override
    public EntityTraversal<Vertex, Vertex> start(Supplier<EntityTraversalSource> g)
    {
        return g.get().V();
    }

}
