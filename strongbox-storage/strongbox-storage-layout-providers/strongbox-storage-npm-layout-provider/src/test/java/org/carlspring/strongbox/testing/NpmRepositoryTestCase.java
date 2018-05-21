package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.storage.repository.NpmRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class NpmRepositoryTestCase
        extends TestCaseWithRepositoryManagement
{

    @Inject
    NpmRepositoryFactory npmRepositoryFactory;


    @Override
    public void createProxyRepository(String storageId,
                                      String repositoryId,
                                      String remoteRepositoryUrl)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        Repository repository = npmRepositoryFactory.createRepository(storageId, repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setLayout(NpmLayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(repository);
    }

}
