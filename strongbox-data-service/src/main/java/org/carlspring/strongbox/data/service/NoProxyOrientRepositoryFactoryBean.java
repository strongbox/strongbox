package org.carlspring.strongbox.data.service;

import javax.inject.Inject;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.orient.commons.repository.support.OrientRepositoryFactoryBean;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

/**
 * @author Alex Oreshkevich
 */
public class NoProxyOrientRepositoryFactoryBean
        extends OrientRepositoryFactoryBean
{

    /**
     * The orient operations.
     */
    @Inject
    private OrientOperations operations;

    @Inject
    private OObjectDatabaseTx databaseTx;

    protected RepositoryFactorySupport doCreateRepositoryFactory()
    {
        return new NoProxyOrientRepositoryFactory(operations, databaseTx);
    }
}
