package org.carlspring.strongbox.data.service;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private OrientOperations operations;

    @Autowired
    private OObjectDatabaseTx databaseTx;

    protected RepositoryFactorySupport doCreateRepositoryFactory()
    {
        return new NoProxyOrientRepositoryFactory(operations, databaseTx);
    }
}
