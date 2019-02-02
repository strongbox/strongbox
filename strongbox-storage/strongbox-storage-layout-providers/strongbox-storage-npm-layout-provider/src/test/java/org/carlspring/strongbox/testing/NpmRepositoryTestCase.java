package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.NpmRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author carlspring
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
public class NpmRepositoryTestCase
        extends TestCaseWithRepositoryManagement
{

    @Inject
    NpmRepositoryFactory npmRepositoryFactory;

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;


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

        MutableRepository repository = npmRepositoryFactory.createRepository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setLayout(NpmLayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(storageId, repository);
    }

    public File getRepositoryBasedir(String storageId, String repositoryId)
    {
        return new File(configurationResourceResolver.getVaultDirectory() + "/storages/" +
                        storageId + "/" + repositoryId);
    }

}
