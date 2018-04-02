package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.repository.NugetRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.carlspring.strongbox.xml.configuration.repository.NugetRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.carmatechnologies.commons.testing.logging.ExpectedLogs;
import com.carmatechnologies.commons.testing.logging.api.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.aristar.jnuget.files.NugetFormatException;
import static org.junit.Assert.assertEquals;
/**
 * @author sbespalov
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
public class NugetGroupRepositoryProviderTest
        extends TestCaseWithNugetPackageGeneration
{

    private static final String REPOSITORY_RELEASES_1 = "grpt-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "grpt-releases-2";
    
    private static final String REPOSITORY_RELEASES_3 = "grpt-releases-3";
    
    private static final String REPOSITORY_GROUP_WITH_NESTED_GROUP_1 = "grpt-releases-group-with-nested-group-level-1";

    private static final String REPOSITORY_GROUP_WITH_NESTED_GROUP_2 = "grpt-releases-group-with-nested-group-level-2";

    private static final String REPOSITORY_GROUP = "grpt-releases-group";

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RepositoryManagementService repositoryManagementService;
    

    @Inject
    private ArtifactManagementService artifactManagementService;

    @Inject
    private NugetRepositoryFactory nugetRepositoryFactory;
    
    @Rule
    public final ExpectedLogs logs = new ExpectedLogs()
    {{
        captureFor(GroupRepositoryProvider.class, LogLevel.DEBUG);
    }};


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void setUp()
        throws Exception
    {
        NugetRepositoryConfiguration nugetRepositoryConfiguration = new NugetRepositoryConfiguration();

        //REPOSITORY_RELEASES_1
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1), NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_1, "grpt.search.package", 9);

        //REPOSITORY_RELEASES_2
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2),
                         NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_2, "grpt.search.package", 12);
        
        //REPOSITORY_RELEASES_3
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_3),
                         NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_3, "grpt.search.package", 8);

        Repository repositoryGroup = nugetRepositoryFactory.createRepository(STORAGE0, REPOSITORY_GROUP);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_1);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_2);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_3);

        createRepository(repositoryGroup);

        Repository repositoryWithNestedGroupLevel1 = nugetRepositoryFactory.createRepository(STORAGE0,
                                                                                             REPOSITORY_GROUP_WITH_NESTED_GROUP_1);
        repositoryWithNestedGroupLevel1.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel1.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel1.setAllowsDelete(false);
        repositoryWithNestedGroupLevel1.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel1.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryWithNestedGroupLevel1.addRepositoryToGroup(REPOSITORY_GROUP);

        createRepository(repositoryWithNestedGroupLevel1);

        Repository repositoryWithNestedGroupLevel2 = nugetRepositoryFactory.createRepository(STORAGE0,
                                                                                             REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryWithNestedGroupLevel2.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel2.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel2.setAllowsDelete(false);
        repositoryWithNestedGroupLevel2.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel2.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryWithNestedGroupLevel2.addRepositoryToGroup(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);

        createRepository(repositoryWithNestedGroupLevel2);
    }

    private void generateRepositoryPackages(String storageId, String repositoryId, int count)
            throws NoSuchAlgorithmException,
                   NugetFormatException,
                   JAXBException,
                   IOException,
                   ProviderImplementationException,
                   ArtifactCoordinatesValidationException
    {
        for (int i = 1; i <= count; i++)
        {
            String packageId = String.format("grpt.search.p%s", i);
            String packageVersion = "1.0.0";
            NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates(packageId, packageVersion, "nupkg");
            Path packageFilePath = generatePackageFile(packageId, packageVersion);
            try (InputStream is = new BufferedInputStream(Files.newInputStream(packageFilePath)))
            {
                artifactManagementService.validateAndStore(storageId,
                                                           repositoryId,
                                                           coordinates.toPath(),
                                                           is);
            }
        }
    }

    private void createRepository(Repository repository)
        throws Exception
    {
        createRepository(repository, repository.getLayout());
    }
    
    private void createRepository(Repository repository, String layout) throws Exception
    {
        repository.setLayout(layout);
        configurationManagementService.saveRepository(repository.getStorage().getId(), repository);
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());        
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_3));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP));

        return repositories;
    }

    @Test
    public void testGroupSearch()
    {
        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        
        RepositorySearchRequest searchRequest = new RepositorySearchRequest(STORAGE0, REPOSITORY_GROUP);
        RepositoryPageRequest pageRequest = new RepositoryPageRequest();
        pageRequest.setSkip(10);
        pageRequest.setLimit(10);
        
        List<Path> result = repositoryProvider.search(searchRequest, pageRequest);
        
        assertEquals(2, result.size());
        
        pageRequest.setLimit(-1);
        result = repositoryProvider.search(searchRequest, pageRequest);
        
        assertEquals(2, result.size());
        
        repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        searchRequest = new RepositorySearchRequest(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        pageRequest.setSkip(11);
        pageRequest.setLimit(10);
        result = repositoryProvider.search(searchRequest, pageRequest);
        
        assertEquals(1, result.size());
        
        pageRequest.setLimit(-1);
        result = repositoryProvider.search(searchRequest, pageRequest);
        
        assertEquals(1, result.size());
        
        Long count = repositoryProvider.count(searchRequest);
        assertEquals(Long.valueOf(12), count);
    }

}
