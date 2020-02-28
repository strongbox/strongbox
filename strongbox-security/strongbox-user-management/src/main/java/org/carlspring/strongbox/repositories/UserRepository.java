package org.carlspring.strongbox.repositories;

import java.util.List;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;
import org.carlspring.strongbox.users.dto.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends GremlinVertexRepository<User>
        implements UserQueries
{

    @Inject
    UserQueries queries;

    public UserRepository()
    {
        super(User.class);
    }

    @Override
    protected EntityTraversalAdapter<Vertex, User> adapter()
    {
        return null;
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

    List<User> findUsersWithRole(String role);
}
