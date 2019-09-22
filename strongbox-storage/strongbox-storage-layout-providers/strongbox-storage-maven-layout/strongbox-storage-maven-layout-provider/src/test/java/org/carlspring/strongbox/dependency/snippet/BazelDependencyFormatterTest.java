package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Declan-Y
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)

public class BazelDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    @Test
    public void testBazelDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(
                Maven2LayoutProvider.ALIAS, BazelDependencyFormatter.ALIAS);

        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertThat("maven_jar(\n    name = \"maven-snippet\","
                     + "\n    artifact = \"org.carlspring.strongbox:maven-snippet:1.0\",\n)\n")
                .as("Failed to generate dependency!")
                .isEqualTo(snippet);
    }

    @Test
    public void testBazelDependencyGenerationWithoutArtifactField()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(
                Maven2LayoutProvider.ALIAS, BazelDependencyFormatter.ALIAS);

        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates()
        {
            @Override
            public String getArtifactId()
            {
                return "maven-snippet";
            }
        };

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertThat("maven_jar(\n    name = \"maven-snippet\",\n)\n")
                .as("Failed to generate dependency!").isEqualTo(snippet);
    }


}
