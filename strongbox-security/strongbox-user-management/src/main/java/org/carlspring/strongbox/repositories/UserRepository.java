package org.carlspring.strongbox.repositories;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.carlspring.strongbox.db.schema.Vertices;
import org.carlspring.strongbox.domain.User;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.adapters.UserAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class UserRepository extends GremlinVertexRepository<User>
        implements UserQueries
{

    @Inject
    UserQueries queries;

    @Inject
    UserAdapter adapter;

    @Override
    protected EntityTraversalAdapter<Vertex, User> adapter()
    {
        return adapter;
    }

    public List<User> findUsersWithRole(String role)
    {
        return queries.findUsersWithRole(role);
    }

    @Override
    public Iterable<User> findAll()
    {
        return findAllUsers();
    }

    @Override
    public List<User> findAllUsers()
    {
        return g().V()
                  .hasLabel(Vertices.USER)
                  .map(adapter.fold())
                  .toList();
    }

}

@Repository
interface UserQueries
        extends org.springframework.data.repository.Repository<User, String>
{

    @Query("MATCH (user:User)-[:UserHasUserRoles]->(userRole:UserRole) " +
            "WHERE userRole.uuid=$role " +
            "RETURN user,userRole")
    List<User> findUsersWithRole(@Param("role") String role);

    @Query("MATCH (user:User)" +
            "RETURN user")
    List<User> findAllUsers();

}
