package org.carlspring.strongbox.storage.indexing;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.repository.RepositoryFeatures;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithRepository;
import org.junit.After;
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

    private static final String REPOSITORY_RELEASES = "m2pr-releases";

    private static final String REPOSITORY_PROXY = "m2pr-proxied-releases";

    @Inject
    private ConfigurationManager configurationManager;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY));

        return repositories;
    }

    @Before
    public void initialize()
            throws Exception
    {
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
    }

    @Test
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, IOException
    {
        Storage storage = configurationManager.getConfiguration().getStorage("nuget-common-storage");
        Repository repository = storage.getRepository("nuget.org");

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        RepositoryFeatures features = layoutProvider.getRepositoryFeatures();
        
        features.downloadRemoteIndex(storage.getId(), repository.getId());
    }

}
