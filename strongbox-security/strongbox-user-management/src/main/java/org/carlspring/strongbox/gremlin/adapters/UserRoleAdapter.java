package org.carlspring.strongbox.gremlin.adapters;

import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.extractObject;

import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.UserRole;
import org.carlspring.strongbox.domain.UserRoleEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

/**
 * @author ankit.tomar
 */
@Component
public class UserRoleAdapter implements VertexEntityTraversalAdapter<UserRole>
{

    @Override
    public String label()
    {
        return Vertices.SECURITY_ROLE;
    }

    @Override
    public EntityTraversal<Vertex, UserRole> fold()
    {
        return __.<Vertex, Object>project("id", "uuid")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .map(this::map);
    }

    private UserRole map(Traverser<Map<String, Object>> t)
    {
        UserRoleEntity result = new UserRoleEntity();
        result.setNativeId(extractObject(Long.class, t.get().get("id")));
        result.setUuid(extractObject(String.class, t.get().get("uuid")));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(UserRole entity)
    {
        return new UnfoldEntityTraversal<>(Vertices.SECURITY_ROLE, entity, __.identity());
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>identity().map(t -> Element.class.cast(t.get()));
    }

}
