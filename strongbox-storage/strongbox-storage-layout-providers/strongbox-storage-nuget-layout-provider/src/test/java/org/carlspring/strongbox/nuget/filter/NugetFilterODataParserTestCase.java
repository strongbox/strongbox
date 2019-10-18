package org.carlspring.strongbox.nuget.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Path;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.QueryTemplate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
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
public class NugetFilterODataParserTestCase
{

    private static final String REPOSITORY_RELEASES = "nfodpt-releases";
    @PersistenceContext
    private EntityManager entityManager;

    
    /**
     * Tests that conjunction works
     */
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @Transactional
    public void testSearchConjunction(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
                                      Repository repository,
                                      @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
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

        Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);
        NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
                "tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt' and IsLatestVersion and Version eq '1.0.8'F");
        Predicate predicate = t.parseQuery().getPredicate();

        selector.where(predicate)
                .and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

        selector.select("count(*)");

        QueryTemplate<Long, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
        assertThat(queryTemplate.select(selector)).isEqualTo(1L);
    }

	/**
	 * Tests that All Chars that should work do
	 */
	@ExtendWith({ RepositoryManagementTestExecutionListener.class,
				  ArtifactManagementTestExecutionListener.class })
	@Test
	@Transactional
	public void testSearchChars(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
										Repository repository,
								@NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
										id = "Org.Carlspring.Strongbox.Nuget.Test.Nfpt_-test",
										versions = { "1.0.0",
													 "1.0.1",
													 "1.0.2"})
										Path artifactPath)
	{

		Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

		NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
				"tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt_-test' and IsLatestVersion");
		Predicate predicate = t.parseQuery().getPredicate();

		selector.where(predicate)
				.and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
				.and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

		selector.select("count(*)");

		QueryTemplate<Long, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
		assertThat(queryTemplate.select(selector)).isEqualTo(1L);
	}

	/*
	 * Tests that starting dash doesn't work
	 */
	@ExtendWith({ RepositoryManagementTestExecutionListener.class,
				  ArtifactManagementTestExecutionListener.class })
	@Test
	@Transactional
	public void testSearchCharsStart(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
											 Repository repository,
									 @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
											 id = "-Org.Carlspring.Strongbox.Nuget.Test.Nfpt",
											 versions = { "1.0.0",
														  "1.0.1",
														  "1.0.2"})
											 Path artifactPath)
	{

		Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

		NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
				"tolower(Id) eq '-org.carlspring.strongbox.nuget.test.nfpt'");
		Predicate predicate = t.parseQuery().getPredicate();

		selector.where(predicate)
				.and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
				.and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

		selector.select("count(*)");

		QueryTemplate<Long, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
		assertThat(queryTemplate.select(selector)).isEqualTo(0L);
	}

	/*
	 * Tests that ending dash doesn't work
	 */
	@ExtendWith({ RepositoryManagementTestExecutionListener.class,
				  ArtifactManagementTestExecutionListener.class })
	@Test
	@Transactional
	public void testSearchCharsEnd(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
										   Repository repository,
								   @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
										   id = "Org.Carlspring.Strongbox.Nuget.Test.Nfpt-",
										   versions = { "1.0.0",
														"1.0.1",
														"1.0.2"})
										   Path artifactPath)
	{

		Selector<ArtifactEntry> selector = new Selector<>(ArtifactEntry.class);

		NugetODataFilterQueryParser t = new NugetODataFilterQueryParser(
				"tolower(Id) eq 'org.carlspring.strongbox.nuget.test.nfpt-'");
		Predicate predicate = t.parseQuery().getPredicate();

		selector.where(predicate)
				.and(Predicate.of(ExpOperator.EQ.of("storageId", repository.getStorage().getId())))
				.and(Predicate.of(ExpOperator.EQ.of("repositoryId", repository.getId())));

		selector.select("count(*)");

		QueryTemplate<Long, ArtifactEntry> queryTemplate = new OQueryTemplate<>(entityManager);
		assertThat(queryTemplate.select(selector)).isEqualTo(0L);
	}

}
