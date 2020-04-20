package org.carlspring.strongbox.repositories;

import java.util.List;

import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.structure.Vertex;
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
        //return queries.findAllUsers()
        //              .stream()
        //              .filter(user -> user.getRoles().contains(role))
        //              .collect(Collectors.toList());
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


}

@Repository
interface UserQueries
        extends org.springframework.data.repository.Repository<User, String>
{

    // TODO :: find solution to query collection datatype
    @Query("MATCH (user:User) " +
           "WHERE $role IN (user.roles) " +
           "RETURN user")
    List<User> findUsersWithRole(@Param("role") String role);

    @Query("MATCH (user:User) " +
           "RETURN user")
    List<User> findAllUsers();

}
