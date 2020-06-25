package org.carlspring.strongbox.repositories;

import org.carlspring.strongbox.domain.UserRole;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdapter;
import org.carlspring.strongbox.gremlin.adapters.UserRoleAdapter;
import org.carlspring.strongbox.gremlin.repositories.GremlinVertexRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.stereotype.Repository;

/**
 * @author ankit.tomar
 */
@Repository
@Transactional
public class UserRoleRepository extends GremlinVertexRepository<UserRole>
        implements UserRoleQueries
{
    @Inject
    private UserRoleQueries queries;

    @Inject
    private UserRoleAdapter userAdapter;

    @Override
    public List<UserRole> findAllUserRoles()
    {
        return queries.findAllUserRoles();
    }

    @Override
    protected EntityTraversalAdapter<Vertex, UserRole> adapter()
    {
        return userAdapter;
    }

}

@Repository
interface UserRoleQueries
        extends org.springframework.data.repository.Repository<UserRole, String>
{

    @Query("MATCH (userRole:UserRole) " +
            "RETURN userRole")
    List<UserRole> findAllUserRoles();

}