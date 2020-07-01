package org.carlspring.strongbox.repositories;

import org.carlspring.strongbox.domain.User;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.adapters.UserAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;
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
        return queries.findAllUsers();
    }

    public List<User> findUsers(Long skip,
                                Integer limit)
    {
        return queries.findUsers(skip, limit);
    }

}

@Repository
interface UserQueries
        extends org.springframework.data.repository.Repository<User, String>
{

    @Query("MATCH (user:User)-[r]->(securityRole:SecurityRole) " +
           "WHERE securityRole.uuid=$role " +
           "RETURN user, r, securityRole")
    List<User> findUsersWithRole(@Param("role") String role);

    @Query("MATCH (user:User)-[r]->(securityRole:SecurityRole) " +
           "RETURN user, r, securityRole")
    List<User> findAllUsers();

    @Query("MATCH (user:User)-[r]->(securityRole:SecurityRole) " +
           "RETURN user, r, securityRole " +
           "ORDER BY user.uuid " +
           "SKIP $skip LIMIT $limit")
    List<User> findUsers(@Param("skip") Long skip,
                         @Param("limit") Integer limit);

}
