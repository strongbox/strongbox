package org.carlspring.strongbox.nuget.filter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Projection;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryLayoutEnum;
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

    @Inject
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
        createRepository(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1),
                         RepositoryLayoutEnum.NUGET.getLayout());
        generateRepositoryPackages(STORAGE0, REPOSITORY_RELEASES_1, "org.carlspring.strongbox.nuget.test.nfpt", 9);

    }

    private void createRepository(Repository repository,
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

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));

        return repositories;
    }

    @Test
    @Transactional
    public void testSearchConjunction()
        throws Exception
    {
        
        Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);
        
        CodePointCharStream is = CharStreams.fromString("tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt' and IsLatestVersion and Version eq '1.0.8'");
        NugetODataFilterLexer lexer = new NugetODataFilterLexer(is);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        NugetODataFilterParser parser = new NugetODataFilterParser(commonTokenStream);

        NugetODataFilterParser.FilterContext fileContext = parser.filter();
        NugetODataFilterVisitor<Predicate> visitor = new NugetODataFilterVisitorImpl(Predicate.empty());
        Predicate predicate = visitor.visitFilter(fileContext);

        selector.where(predicate)
                .and(Predicate.of(ExpOperator.EQ.of("storageId", STORAGE0)))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", REPOSITORY_RELEASES_1)));
        
        selector.select("count(*)");
        
        QueryTemplate queryTemplate = new OQueryTemplate(entityManager);
        Assert.assertEquals(Long.valueOf(1), queryTemplate.select(selector));
    }

}
