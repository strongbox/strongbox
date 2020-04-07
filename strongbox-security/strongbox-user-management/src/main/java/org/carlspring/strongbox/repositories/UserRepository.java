package org.carlspring.strongbox.repositories;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.adapters.UserAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.carlspring.strongbox.users.dto.User;
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

}

@Repository
interface UserQueries
        extends org.springframework.data.repository.Repository<User, String>
{

    default List<User> findUsersWithRole(String role)
    {
        return null;
    }
}
