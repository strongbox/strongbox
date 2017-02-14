package org.carlspring.strongbox.data.service;

import java.io.Serializable;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.data.orient.commons.core.OrientOperations;
import org.springframework.data.orient.commons.repository.support.OrientRepositoryFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;

/**
 * @author Alex Oreshkevich
 */
public class NoProxyOrientRepositoryFactory
        extends OrientRepositoryFactory
{

    private EntityManagerFactory entityManagerFactory;

    public NoProxyOrientRepositoryFactory(OrientOperations operations,
                                          EntityManagerFactory entityManagerFactory)
    {
        super(operations);
        this.entityManagerFactory = entityManagerFactory;

        if (entityManagerFactory == null)
        {
            throw new BeanCreationException("Unable to inject databaseTx.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object getTargetRepository(RepositoryInformation metadata)
    {
        EntityInformation<?, Serializable> entityInformation = getEntityInformation(metadata.getDomainType());
        Class<?> repositoryInterface = metadata.getRepositoryInterface();
        Class<?> javaType = entityInformation.getJavaType();
        String cluster = getCustomCluster(metadata);

        NoProxyRepositoryImpl repository;

        if (cluster != null)
        {
            repository = new NoProxyRepositoryImpl(operations, javaType, cluster, repositoryInterface);
        }
        else
        {
            repository = new NoProxyRepositoryImpl(operations, javaType, repositoryInterface);
        }

        repository.setDatabaseTx(entityManagerFactory);

        return repository;
    }


    /* (non-Javadoc)
     * @see org.springframework.data.repository.core.support.RepositoryFactorySupport#getRepositoryBaseClass(org.springframework.data.repository.core.RepositoryMetadata)
     */
    @Override
    protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata)
    {
        return NoProxyRepositoryImpl.class;
    }
}
