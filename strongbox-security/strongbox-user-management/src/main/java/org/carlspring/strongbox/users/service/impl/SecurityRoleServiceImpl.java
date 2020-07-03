package org.carlspring.strongbox.users.service.impl;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.domain.SecurityRole;
import org.carlspring.strongbox.domain.SecurityRoleEntity;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.repositories.SecurityRoleRepository;
import org.carlspring.strongbox.users.service.SecurityRoleService;

import javax.inject.Inject;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraph;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ankit.tomar
 */
@Service
@Transactional
public class SecurityRoleServiceImpl implements SecurityRoleService
{

    @Inject
    private JanusGraph janusGraph;

    @Inject
    private SecurityRoleRepository securityRoleRepository;

    @Override
    @Cacheable(value = CacheName.User.SECURITY_ROLES, key = "#roleName", sync = true)
    public SecurityRole findOneOrCreate(String roleName)
    {
        Optional<SecurityRole> securityRole = securityRoleRepository.findById(roleName);

        return securityRole.orElseGet(() -> {

            Graph g = janusGraph.tx().createThreadedTx();
            try
            {
                SecurityRoleEntity userRoleEntity = securityRoleRepository.save(() -> g.traversal(EntityTraversalSource.class),
                                                                                new SecurityRoleEntity(roleName));
                g.tx().commit();

                return userRoleEntity;
            }
            catch (Exception e)
            {
                g.tx().rollback();
                throw new UndeclaredThrowableException(e);
            } 
            finally
            {
                g.tx().close();
            }

        });
    }
}
