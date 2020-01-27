package org.carlspring.strongbox.nuget.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
@Disabled
public class NugetFilterODataParserTest
{

    private static final String REPOSITORY_RELEASES_1 = "nfodpt-releases-1";
    
    private static final String REPOSITORY_RELEASES_2 = "nfodpt-releases-2";
    
    private static final String REPOSITORY_RELEASES_3 = "nfodpt-releases-3";
    
    private static final String REPOSITORY_RELEASES_4 = "nfodpt-releases-4";
    
    //@PersistenceContext
    private EntityManager entityManager;


    /**
     * Tests that conjunction works
     */
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @Transactional
    public void testSearchConjunction(@NugetRepository(repositoryId = REPOSITORY_RELEASES_1)
                                      Repository repository,
                                      @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                         id = "Org.Carlspring.Strongbox.Nuget.Test.Nfpt",
                                                         versions = { "1.0.0",
                                                                      "1.0.1",
                                                                      "1.0.2",
                                                                      "1.0.3",
                                                                      "1.0.4",
                                                                      "1.0.5",
                                                                      "1.0.6",
                                                                      "1.0.7",
                                                                      "1.0.8"})
                                      Path artifactPath)
    {
        assertThat(Files.exists(artifactPath));
        Selector<ArtifactEntity> selector = new Selector<>(ArtifactEntity.class);
        NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
                "tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt' and IsLatestVersion and Version eq '1.0.8'");
        Predicate predicate = t.parseQuery().getPredicate();

        selector.where(predicate)
                .and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

        selector.select("COUNT(*)");

        QueryTemplate<Long, ArtifactEntity> queryTemplate = new OQueryTemplate<>(entityManager);
        
        assertThat(((OQueryTemplate<Long, ArtifactEntity>) queryTemplate).calculateQueryString(selector)).isEqualTo("SELECT COUNT(*) FROM ArtifactEntry WHERE " +
                                                                                                                   "artifactCoordinates.coordinates.id.toLowerCase() = :id_0 AND tagSet CONTAINS (name = :name_1) AND " +
                                                                                                                   "artifactCoordinates.coordinates.version = :version_1 AND storageId = :storageId_1 AND repositoryId = :repositoryId_2 LIMIT 1000");
        
        Map<String, Object> parameterMap = ((OQueryTemplate<Long, ArtifactEntity>) queryTemplate).exposeParameterMap(selector.getPredicate());

        assertThat(parameterMap.get("id_0")).isEqualTo("org.carlspring.strongbox.nuget.test.nfpt");
        assertThat(parameterMap.get("version_1")).isEqualTo("1.0.8");
        assertThat(parameterMap.get("name_1")).isEqualTo("last-version");
        assertThat(parameterMap.get("storageId_1")).isEqualTo("storage-nuget");
        assertThat(parameterMap.get("repositoryId_2")).isEqualTo("nfodpt-releases-1");
    }

    /**
     * Tests that All Chars that should work do
     */
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @Transactional
    public void testSearchChars(@NugetRepository(repositoryId = REPOSITORY_RELEASES_2)
                                Repository repository,
                                @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                                                   id = "Org.Carlspring.Strongbox.Nuget.Test.Nfpt_-test",
                                                   versions = { "1.0.0",
                                                                "1.0.1",
                                                                "1.0.2"})
                                Path artifactPath)
    {
        Selector<ArtifactEntity> selector = new Selector<>(ArtifactEntity.class);

        NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
                "tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt_-test' and IsLatestVersion");
        Predicate predicate = t.parseQuery().getPredicate();

        selector.where(predicate)
                .and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

        selector.select("artifactCoordinates.coordinates.id");

        QueryTemplate<String, ArtifactEntity> queryTemplate = new OQueryTemplate<>(entityManager);
        assertThat(queryTemplate.select(selector)).isEqualTo("Org.Carlspring.Strongbox.Nuget.Test.Nfpt_-test");
    }
}
