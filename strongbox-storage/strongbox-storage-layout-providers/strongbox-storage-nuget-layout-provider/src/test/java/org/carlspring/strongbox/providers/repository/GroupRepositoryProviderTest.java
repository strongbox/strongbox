package org.carlspring.strongbox.providers.repository;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.artifact.coordinates.NugetHierarchicalArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NugetPackageGenerator;
import org.carlspring.strongbox.config.NugetLayoutProviderConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.PropertyUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.carlspring.strongbox.xml.configuration.repository.NugetRepositoryConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.carmatechnologies.commons.testing.logging.ExpectedLogs;
import com.carmatechnologies.commons.testing.logging.api.LogLevel;

import ru.aristar.jnuget.files.NugetFormatException;

/**
 * @author sbespalov
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NugetLayoutProviderConfig.class)
public class GroupRepositoryProviderTest
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
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1),
                         RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_1, 9);

        //REPOSITORY_RELEASES_2
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2),
                         RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_2, 12);
        
        //REPOSITORY_RELEASES_3
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_3),
                         RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_3, 8);

        Repository repositoryGroup = new Repository(REPOSITORY_GROUP);
        repositoryGroup.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setLayout(RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_1);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_2);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_3);

        createRepository(repositoryGroup);

        Repository repositoryWithNestedGroupLevel1 = new Repository(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);
        repositoryWithNestedGroupLevel1.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repositoryWithNestedGroupLevel1.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel1.setLayout(RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
        repositoryWithNestedGroupLevel1.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel1.setAllowsDelete(false);
        repositoryWithNestedGroupLevel1.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel1.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryWithNestedGroupLevel1.addRepositoryToGroup(REPOSITORY_GROUP);

        createRepository(repositoryWithNestedGroupLevel1);

        Repository repositoryWithNestedGroupLevel2 = new Repository(REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryWithNestedGroupLevel2.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repositoryWithNestedGroupLevel2.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel2.setLayout(RepositoryLayoutEnum.NUGET_HIERARCHICAL.getLayout());
        repositoryWithNestedGroupLevel2.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel2.setAllowsDelete(false);
        repositoryWithNestedGroupLevel2.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel2.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryWithNestedGroupLevel2.addRepositoryToGroup(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);

        createRepository(repositoryWithNestedGroupLevel2);
    }

    private void generateRepositoryPackages(String storageId,
                                            String repositoryId,
                                            int count)
        throws NoSuchAlgorithmException,
        NugetFormatException,
        JAXBException,
        IOException,
        ProviderImplementationException
    {
        for (int i = 1; i <= count; i++)
        {
            String packageId = String.format("grpt.search.p%s", i);
            String packageVersion = "1.0.0";
            NugetHierarchicalArtifactCoordinates coordinates = new NugetHierarchicalArtifactCoordinates(packageId, packageVersion, "nupkg");
            Path packageFilePath = generatePackageFile(packageId, packageVersion);
            artifactManagementService.validateAndStore(storageId, repositoryId, coordinates.toPath(),
                                                       Files.newInputStream(packageFilePath));
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
            throws Exception
    {
        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        RepositorySearchRequest request = new RepositorySearchRequest(STORAGE0, REPOSITORY_GROUP);
        request.setSkip(10);
        request.setLimit(10);
        List<Path> result = repositoryProvider.search(request);
        
        assertEquals(2, result.size());
        
        request.setLimit(-1);
        result = repositoryProvider.search(request);
        
        assertEquals(2, result.size());
        
        repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        request = new RepositorySearchRequest(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        request.setSkip(11);
        request.setLimit(10);
        result = repositoryProvider.search(request);
        
        assertEquals(1, result.size());
        
        request.setLimit(-1);
        result = repositoryProvider.search(request);
        
        assertEquals(1, result.size());
    }

}
