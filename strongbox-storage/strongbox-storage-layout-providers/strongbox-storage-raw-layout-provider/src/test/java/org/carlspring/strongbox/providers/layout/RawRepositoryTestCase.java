package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.config.RawLayoutProviderTestConfig;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RawRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithRepositoryManagement;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = RawLayoutProviderTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
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
        MutableRemoteRepository remoteRepository = new MutableRemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        MutableRepository repository = rawRepositoryFactory.createRepository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setLayout(RawLayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(storageId, repository);
    }

}
