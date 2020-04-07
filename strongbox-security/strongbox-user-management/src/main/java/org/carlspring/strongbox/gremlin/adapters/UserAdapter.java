package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.set;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extracPropertytList;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.extractObject;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.toLocalDateTime;
import static org.carlspring.strongbox.gremlin.adapters.EntityTraversalUtils.toLong;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.UserEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.__;
import org.carlspring.strongbox.users.dto.User;
import org.springframework.stereotype.Component;

/**
 * @author sbespalov
 */
@Component
public class UserAdapter extends VertexEntityTraversalAdapter<User>
{

    @Override
    public Set<String> labels()
    {
        return Collections.singleton(Vertices.USER);
    }

    @Override
    public EntityTraversal<Vertex, User> fold()
    {
        return __.<Vertex, Object>project("id", "uuid", "password", "enabled", "roles", "securityTokenKey", "lastUpdated", "sourceId")
                 .by(__.id())
                 .by(__.enrichPropertyValue("uuid"))
                 .by(__.enrichPropertyValue("password"))
                 .by(__.enrichPropertyValue("enabled"))
                 .by(__.enrichPropertyValue("roles"))
                 .by(__.enrichPropertyValue("securityTokenKey"))
                 .by(__.enrichPropertyValue("lastUpdated"))
                 .by(__.enrichPropertyValue("sourceId"))
                 .map(this::map);
    }

    private User map(Traverser<Map<String, Object>> t)
    {
        UserEntity result = new UserEntity(extractObject(String.class, t.get().get("uuid")));
        result.setNativeId(extractObject(Long.class, t.get().get("id")));

        result.setPassword(extractObject(String.class, t.get().get("password")));
        result.setEnabled(extractObject(Boolean.class, t.get().get("enabled")));
        result.setRoles(extracPropertytList(String.class, t.get().get("roles")).stream()
                                                                               .filter(e -> !e.trim().isEmpty())
                                                                               .collect(Collectors.toSet()));
        result.setSecurityTokenKey(extractObject(String.class, t.get().get("securityTokenKey")));
        result.setLastUpdated(toLocalDateTime(extractObject(Long.class, t.get().get("lastUpdated"))));
        result.setSourceId(extractObject(String.class, t.get().get("sourceId")));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(User entity)
    {
        EntityTraversal<Vertex, Vertex> t = __.<Vertex>identity();

        if (entity.getPassword() != null)
        {
            t = t.property(single, "password", entity.getPassword());
        }
        if (entity.getSecurityTokenKey() != null)
        {
            t = t.property(single, "securityTokenKey", entity.getSecurityTokenKey());
        }
        if (entity.getSourceId() != null)
        {
            t = t.property(single, "sourceId", entity.getSourceId());
        }
        if (entity.getLastUpdated() != null)
        {
            t = t.property(single, "lastUpdated", toLong(entity.getLastUpdated()));
        }

        t = t.property(single, "enabled", entity.isEnabled());

        Set<String> roles = entity.getRoles();
        t = t.sideEffect(__.properties("roles").drop());
        t = t.property(set, "roles", "");
        for (String role : roles)
        {
            t = t.property(set, "roles", role);
        }

        return new UnfoldEntityTraversal<>(Vertices.USER, t);
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>identity().map(t -> Element.class.cast(t.get()));
    }

}
