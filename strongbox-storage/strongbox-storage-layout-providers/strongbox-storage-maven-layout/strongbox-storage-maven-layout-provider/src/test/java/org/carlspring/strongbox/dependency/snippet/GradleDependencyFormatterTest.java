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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class GradleDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @Test
    public void testGradleDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            GradleDependencyFormatter.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("compile \"org.carlspring.strongbox:maven-snippet:1.0\"\n");
    }

    @Test
    public void testGradleDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            GradleDependencyFormatter.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        // compile "org.gradle.test.classifiers:service:1.0:jdk15@jar"
        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("compile \"org.carlspring.strongbox:maven-snippet:1.0:jdk12\"\n");
    }

    @Test
    public void testGradleDependencyGenerationWithExtensionAndClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            GradleDependencyFormatter.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("zip");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        // compile "org.gradle.test.classifiers:service:1.0:jdk15@jar"
        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("compile \"org.carlspring.strongbox:maven-snippet:1.0:jdk12@zip\"\n");
    }

}
