package org.carlspring.strongbox.nuget.filter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
public class NugetFilterODataParserTestCase extends TestCaseWithNugetPackageGeneration
{

    private static final String REPOSITORY_RELEASES_1 = "nfpt-releases-1";

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @PersistenceContext
    private EntityManager entityManager;

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
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, NugetLayoutProvider.ALIAS),
                         NugetLayoutProvider.ALIAS);
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_1, "Org.Carlspring.Strongbox.Nuget.Test.Nfpt", 9);

    }

    private void createRepository(MutableRepository repository,
                                  String layout)
        throws Exception
    {
        repository.setLayout(layout);
        configurationManagementService.saveRepository(repository.getStorage().getId(), repository);
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());
    }

    @After
    public void removeRepositories()
        throws IOException,
        JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, NugetLayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    @Transactional
    public void testSearchConjunction()
        throws Exception
    {

        Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

        NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
                "tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt' and IsLatestVersion and Version eq '1.0.8'F");
        Predicate predicate = t.parseQuery().getPredicate();

        selector.where(predicate)
                .and(Predicate.of(ExpOperator.EQ.of("storageId", STORAGE0)))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", REPOSITORY_RELEASES_1)));

        selector.select("count(*)");

        QueryTemplate<Long, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
        Assert.assertEquals(Long.valueOf(1), queryTemplate.select(selector));
    }

}
