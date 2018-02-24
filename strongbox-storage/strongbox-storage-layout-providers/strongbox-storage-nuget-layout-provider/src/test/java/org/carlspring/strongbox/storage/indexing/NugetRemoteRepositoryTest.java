package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.nuget.NugetSearchRequest;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.NugetRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithRepository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * @author Sergey Bespalov
 *
 */
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class NugetRemoteRepositoryTest
        extends TestCaseWithRepository
{

    private static final String NUGET_COMMON_STORAGE = "storage-nuget";

    private static final String REPOSITORY_PROXY = "nrrt-proxy";

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private NugetRepositoryFeatures features;

    @Inject
    private NugetRepositoryFactory nugetRepositoryFactory;

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
        Repository repository = nugetRepositoryFactory.createRepository(NUGET_COMMON_STORAGE, REPOSITORY_PROXY);
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
            configurationManagementService.removeRepository(repository.getStorage().getId(), repository.getId());
        }
    }

    @Test
    public void testRepositoryIndexFetching()
        throws ArtifactTransportException,
        IOException
    {
        Storage storage = configurationManager.getConfiguration().getStorage(NUGET_COMMON_STORAGE);
        Repository repository = storage.getRepository(REPOSITORY_PROXY);

        NugetSearchRequest nugetSearchRequest = new NugetSearchRequest();
        nugetSearchRequest.setFilter(String.format("Id eq '%s'", "NHibernate"));
        
        features.downloadRemoteFeed(storage.getId(),
                                    repository.getId(),
                                    nugetSearchRequest);

        NugetArtifactCoordinates c = new NugetArtifactCoordinates("NHibernate", "4.0.4.4000", "nupkg");
        Optional<ArtifactEntry> artifactEntry = artifactEntryService.findOneArtifact(NUGET_COMMON_STORAGE,
                                                                                     REPOSITORY_PROXY,
                                                                                     c.toPath());

        Assert.assertTrue(artifactEntry.isPresent());
        Assert.assertFalse(((RemoteArtifactEntry) artifactEntry.get()).getIsCached());
    }

}
