package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NugetTestArtifact;
import org.carlspring.strongbox.testing.repository.NugetRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class NugetDependencyFormatterTest
{

    private static final String REPOSITORY_RELEASES = "ndft-releases";

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testGradleDependencyGeneration(@NugetRepository(repositoryId = REPOSITORY_RELEASES)
                                               Repository repository,
                                               @NugetTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                  id = "Org.Carlspring.Strongbox.NuGet.Snippet",
                                                                  versions = "1.0")
                                               Path artifactPath)
            throws ProviderImplementationException, IOException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(
                NugetLayoutProvider.ALIAS,
                NugetDependencyFormatter.ALIAS);
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        RepositoryPath normalizedPath = (RepositoryPath) artifactPath.normalize();
        NugetArtifactCoordinates coordinates = (NugetArtifactCoordinates) RepositoryFiles.readCoordinates(
                normalizedPath);

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("<dependency id=\"" + coordinates.getId() + "\" version=\"" + coordinates.getVersion() + "\" />\n",
                     snippet,
                     "Failed to generate dependency!");
    }

}
