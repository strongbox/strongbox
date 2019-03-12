package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.nuget.NugetFormatException;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.NugetRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidationException;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.carlspring.strongbox.xml.configuration.repository.MutableNugetRepositoryConfiguration;

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author sbespalov
 *
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
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

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @BeforeEach
    public void setUp()
        throws Exception
    {
        MutableNugetRepositoryConfiguration nugetRepositoryConfiguration = new MutableNugetRepositoryConfiguration();

        //REPOSITORY_RELEASES_1
        createRepository(STORAGE0, createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, NugetLayoutProvider.ALIAS), NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_1, "grpt.search.package", 9);

        //REPOSITORY_RELEASES_2
        createRepository(STORAGE0, createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, NugetLayoutProvider.ALIAS),
                         NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_2, "grpt.search.package", 12);
        
        //REPOSITORY_RELEASES_3
        createRepository(STORAGE0, createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_3, NugetLayoutProvider.ALIAS),
                         NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_3, "grpt.search.package", 8);

        MutableRepository repositoryGroup = nugetRepositoryFactory.createRepository(REPOSITORY_GROUP);
        repositoryGroup.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryGroup.setAllowsRedeployment(false);
        repositoryGroup.setAllowsDelete(false);
        repositoryGroup.setAllowsForceDeletion(false);
        repositoryGroup.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_1);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_2);
        repositoryGroup.addRepositoryToGroup(REPOSITORY_RELEASES_3);

        createRepository(STORAGE0, repositoryGroup);

        MutableRepository repositoryWithNestedGroupLevel1 = nugetRepositoryFactory.createRepository(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);
        repositoryWithNestedGroupLevel1.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel1.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel1.setAllowsDelete(false);
        repositoryWithNestedGroupLevel1.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel1.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryWithNestedGroupLevel1.addRepositoryToGroup(REPOSITORY_GROUP);

        createRepository(STORAGE0, repositoryWithNestedGroupLevel1);

        MutableRepository repositoryWithNestedGroupLevel2 = nugetRepositoryFactory.createRepository(REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryWithNestedGroupLevel2.setType(RepositoryTypeEnum.GROUP.getType());
        repositoryWithNestedGroupLevel2.setAllowsRedeployment(false);
        repositoryWithNestedGroupLevel2.setAllowsDelete(false);
        repositoryWithNestedGroupLevel2.setAllowsForceDeletion(false);
        repositoryWithNestedGroupLevel2.setRepositoryConfiguration(nugetRepositoryConfiguration);
        repositoryWithNestedGroupLevel2.addRepositoryToGroup(REPOSITORY_GROUP_WITH_NESTED_GROUP_1);

        createRepository(STORAGE0, repositoryWithNestedGroupLevel2);
    }

    private void createRepository(String storageId, MutableRepository repository)
        throws Exception
    {
        createRepository(storageId, repository, repository.getLayout());
    }
    
    private void createRepository(String storageId, MutableRepository repository, String layout) throws Exception
    {
        repository.setLayout(layout);
        configurationManagementService.saveRepository(storageId, repository);
        repositoryManagementService.createRepository(storageId, repository.getId());
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, NugetLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, NugetLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_3, NugetLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_1, NugetLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2, NugetLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP, NugetLayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testGroupSearch()
    {
        Repository repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP);
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        
        Paginator paginator = new Paginator();
        paginator.setLimit(10);
        paginator.setSkip(10);

        Predicate p = Predicate.empty();
        
        List<Path> result = repositoryProvider.search(STORAGE0, REPOSITORY_GROUP, p, paginator);
        
        assertEquals(2, result.size());
        
        paginator.setLimit(-1);
        result = repositoryProvider.search(STORAGE0, REPOSITORY_GROUP, p, paginator);
        
        assertEquals(2, result.size());
        
        repository = configurationManager.getRepository(STORAGE0 + ":" + REPOSITORY_GROUP_WITH_NESTED_GROUP_2);
        repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());
        
        paginator.setSkip(11);
        paginator.setLimit(10);
        
        result = repositoryProvider.search(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2, p, paginator);
        
        assertEquals(1, result.size());
        
        paginator.setLimit(-1);
        result = repositoryProvider.search(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2, p, paginator);
        
        assertEquals(1, result.size());
        
        Long count = repositoryProvider.count(STORAGE0, REPOSITORY_GROUP_WITH_NESTED_GROUP_2, p);
        assertEquals(Long.valueOf(12), count);
    }

}
