package org.carlspring.strongbox.nuget.filter;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithNugetArtifactGeneration;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
public class NugetFilterODataParserTestCase extends TestCaseWithNugetArtifactGeneration
{

    private static final String REPOSITORY_RELEASES = "nfodpt-releases";

    @PersistenceContext
    private EntityManager entityManager;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @Transactional
    public void testSearchConjunction(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
                                      Repository repository,
                                      @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                         id = "Org.Carlspring.Strongbox.Nuget.Test.Nfpt",
                                                         versions = { "1.0.1",
                                                                      "1.0.2",
                                                                      "1.0.3",
                                                                      "1.0.4",
                                                                      "1.0.5",
                                                                      "1.0.6",
                                                                      "1.0.7",
                                                                      "1.0.8"})
                                      Path artifactPath)
    {

        Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

        NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
                "tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt' and IsLatestVersion and Version eq '1.0.8'F");
        Predicate predicate = t.parseQuery().getPredicate();

        selector.where(predicate)
                .and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

        selector.select("count(*)");

        QueryTemplate<Long, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
        assertEquals(Long.valueOf(1), queryTemplate.select(selector));
    }

}
