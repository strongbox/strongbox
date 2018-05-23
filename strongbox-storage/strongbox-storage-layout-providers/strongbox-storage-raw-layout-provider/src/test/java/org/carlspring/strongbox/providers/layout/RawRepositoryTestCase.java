package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.config.RawLayoutProviderTestConfig;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.storage.repository.RawRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithRepositoryManagement;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = RawLayoutProviderTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class RawRepositoryTestCase
        extends TestCaseWithRepositoryManagement
{

    @Inject
    RawRepositoryFactory rawRepositoryFactory;


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

        Repository repository = rawRepositoryFactory.createRepository(storageId, repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setLayout(RawLayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(repository);
    }

}
