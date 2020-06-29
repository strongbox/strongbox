package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.domain.UserRole;
import org.carlspring.strongbox.domain.UserRoleEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.repositories.UserRoleRepository;
import org.carlspring.strongbox.users.service.UserRoleService;

import javax.inject.Inject;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ankit.tomar
 */
@Service
@Transactional
public class UserRoleServiceImpl implements UserRoleService
{

    @Inject
    private JanusGraph janusGraph;

    @Inject
    private UserRoleRepository userRoleRepository;

    @Override
    public UserRole findOneOrCreate(String role)
    {
        Optional<UserRole> userRole = userRoleRepository.findById(role);

        return userRole.orElseGet(() -> {

            Graph g = janusGraph.tx().createThreadedTx();
            try
            {
                UserRoleEntity userRoleEntity = userRoleRepository.save(() -> g.traversal(EntityTraversalSource.class),
                                                                        new UserRoleEntity(role));
                g.tx().commit();

                return userRoleEntity;
            }
            catch (Exception e)
            {
                g.tx().rollback();
                throw new UndeclaredThrowableException(e);
            } finally
            {
                g.tx().close();
            }
        });
    }
}
