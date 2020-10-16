package org.carlspring.strongbox.gremlin.adapters;

import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.extractObject;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.toLocalDateTime;
import static org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils.toLong;

import org.carlspring.strongbox.db.schema.Edges;
import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.User;
import org.carlspring.strongbox.domain.UserEntity;
import org.carlspring.strongbox.domain.SecurityRole;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversal;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalUtils;
import org.carlspring.strongbox.gremlin.dsl.__;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Component;

import static org.carlspring.strongbox.db.schema.Properties.UUID;
import static org.carlspring.strongbox.db.schema.Properties.PASSWORD;
import static org.carlspring.strongbox.db.schema.Properties.ENABLED;
import static org.carlspring.strongbox.db.schema.Properties.SECURITY_TOKEN_KEY;
import static org.carlspring.strongbox.db.schema.Properties.LAST_UPDATED;
import static org.carlspring.strongbox.db.schema.Properties.SOURCE_ID;

/**
 * @author sbespalov
 */
@Component
public class UserAdapter implements VertexEntityTraversalAdapter<User>
{

    @Inject
    private SecurityRoleAdapter securityRoleAdapter;

    @Override
    public String label()
    {
        return Vertices.USER;
    }

    @Override
    public EntityTraversal<Vertex, User> fold()
    {
        return __.<Vertex, Object>project("id",
        								  UUID,
        								  PASSWORD,
        								  ENABLED,
                                          "roles",
                                          SECURITY_TOKEN_KEY,
                                          LAST_UPDATED,
                                          SOURCE_ID)
                 .by(__.id())
                 .by(__.enrichPropertyValue(UUID))
                 .by(__.enrichPropertyValue(PASSWORD))
                 .by(__.enrichPropertyValue(ENABLED))
                 .by(__.outE(Edges.USER_HAS_SECURITY_ROLES)
                       .inV()
                       .map(securityRoleAdapter.fold())
                       .map(EntityTraversalUtils::castToObject)
                       .fold())
                 .by(__.enrichPropertyValue(SECURITY_TOKEN_KEY))
                 .by(__.enrichPropertyValue(LAST_UPDATED))
                 .by(__.enrichPropertyValue(SOURCE_ID))
                 .map(this::map);
    }

    private User map(Traverser<Map<String, Object>> t)
    {
        UserEntity result = new UserEntity(extractObject(String.class, t.get().get(UUID)));
        result.setNativeId(extractObject(Long.class, t.get().get("id")));

        result.setPassword(extractObject(String.class, t.get().get(PASSWORD)));
        result.setEnabled(extractObject(Boolean.class, t.get().get(ENABLED)));
        List<SecurityRole> userRoles = (List<SecurityRole>) t.get().get("roles");
        result.setRoles(new HashSet<>(userRoles));
        result.setSecurityTokenKey(extractObject(String.class, t.get().get(SECURITY_TOKEN_KEY)));
        result.setLastUpdated(toLocalDateTime(extractObject(Long.class, t.get().get(LAST_UPDATED))));
        result.setSourceId(extractObject(String.class, t.get().get(SOURCE_ID)));

        return result;
    }

    @Override
    public UnfoldEntityTraversal<Vertex, Vertex> unfold(User entity)
    {
        String storedUserId = Vertices.USER + ":" + java.util.UUID.randomUUID().toString();

        EntityTraversal<Vertex, Vertex> userRoleTraversal = __.identity();
        EntityTraversal<Vertex, Vertex> unfoldTraversal = __.identity();

        unfoldTraversal.sideEffect(__.outE(Edges.USER_HAS_SECURITY_ROLES).drop());

        for (SecurityRole securityRole : entity.getRoles())
        {
            userRoleTraversal = userRoleTraversal.V(securityRole)
                                                 .saveV(securityRole.getUuid(),
                                                        securityRoleAdapter.unfold(securityRole));

            userRoleTraversal = userRoleTraversal.addE(Edges.USER_HAS_SECURITY_ROLES)
                                                 .from(__.<Vertex, Vertex>select(storedUserId).unfold())
                                                 .inV();

            userRoleTraversal = userRoleTraversal.inE(Edges.USER_HAS_SECURITY_ROLES).outV();

        }

        unfoldTraversal = unfoldTraversal.map(unfoldUser(entity))
                                         .store(storedUserId)
                                         .map(userRoleTraversal);

        return new UnfoldEntityTraversal<>(Vertices.USER, entity, unfoldTraversal);
    }

    private EntityTraversal<Vertex, Vertex> unfoldUser(User entity)
    {
        EntityTraversal<Vertex, Vertex> t = __.<Vertex>identity();

        if (entity.getPassword() != null)
        {
            t = t.property(single, PASSWORD, entity.getPassword());
        }
        if (entity.getSecurityTokenKey() != null)
        {
            t = t.property(single, SECURITY_TOKEN_KEY, entity.getSecurityTokenKey());
        }
        if (entity.getSourceId() != null)
        {
            t = t.property(single, SOURCE_ID, entity.getSourceId());
        }
        if (entity.getLastUpdated() != null)
        {
            t = t.property(single, LAST_UPDATED, toLong(entity.getLastUpdated()));
        }

        t = t.property(single, ENABLED, entity.isEnabled());

        return t;
    }

    @Override
    public EntityTraversal<Vertex, Element> cascade()
    {
        return __.<Vertex>identity().map(t -> Element.class.cast(t.get()));
    }

}
