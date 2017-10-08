package org.carlspring.strongbox.storage.indexing;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryFeatures;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.aristar.jnuget.query.IdEqIgnoreCase;

/**
 * @author Sergey Bespalov
 *
 */
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class NugetRemoteRepositoryTest
        extends TestCaseWithRepository
{

    private static final String NUGET_COMMON_STORAGE = "nuget-common-storage";
    private static final String REPOSITORY_PROXY = "nrrt-proxy";

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @BeforeClass
    public static void cleanUp()
        throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(NUGET_COMMON_STORAGE, REPOSITORY_PROXY));

        return repositories;
    }

    @Before
    public void initialize()
        throws Exception
    {
        Storage storage = configurationManager.getConfiguration().getStorage(NUGET_COMMON_STORAGE);

        Repository repository = new Repository(REPOSITORY_PROXY);
        repository.setStorage(storage);
        repository.setIndexingEnabled(false);
        repository.setLayout(RepositoryLayoutEnum.NUGET_HIERACHLICAL.getLayout());
        repository.setType("proxy");
        repository.setRemoteRepository(new RemoteRepository());
        repository.getRemoteRepository().setUrl("https://www.nuget.org/api/v2");

        configurationManagementService.saveRepository(repository.getStorage().getId(), repository);
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());
    }

    @After
    public void removeRepositories()
        throws IOException,
        JAXBException
    {
        for (Repository repository : getRepositoriesToClean())
        {
            configurationManagementService.removeRepository(repository.getStorage()
                                                                      .getId(),
                                                            repository.getId());
        }
    }

    @Test
    public void testRepositoryIndexFetching()
        throws ArtifactTransportException,
        IOException
    {
        Storage storage = configurationManager.getConfiguration().getStorage(NUGET_COMMON_STORAGE);
        Repository repository = storage.getRepository(REPOSITORY_PROXY);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryFeatures features = layoutProvider.getRepositoryFeatures();

        ((NugetRepositoryFeatures) features).downloadRemoteFeed(storage.getId(), repository.getId(),
                                                                 new IdEqIgnoreCase("adplug"), null, null);

    }

}
