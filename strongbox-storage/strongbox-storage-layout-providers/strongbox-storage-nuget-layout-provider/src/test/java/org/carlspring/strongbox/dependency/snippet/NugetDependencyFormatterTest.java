package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    @Test
    public void testGradleDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(
                NugetLayoutProvider.ALIAS,
                NugetDependencyFormatter.ALIAS);

        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates("Org.Carlspring.Strongbox.NuGet.Snippet",
                                                                            "1.0");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertThat("<dependency id=\"" + coordinates.getId() + "\" version=\"" + coordinates.getVersion() + "\" />\n")
                .as("Failed to generate dependency!")
                .isEqualTo(snippet);
    }

}
