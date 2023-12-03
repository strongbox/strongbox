package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleksandr Gryniuk
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class BuildrDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    MavenArtifactCoordinates coordinates;

    @BeforeEach
    public void setUp()
    {
        coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
    }

    @Test
    public void testBuildrDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            BuildrDependencyFormatter.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("org.carlspring.strongbox:maven-snippet:jar:1.0");
    }

    @Test
    public void testBuildrDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            BuildrDependencyFormatter.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        coordinates.setExtension("jar");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("org.carlspring.strongbox:maven-snippet:jar:jdk12:1.0");
    }

    @Test
    public void testBuildrDependencyGenerationWithNonStandardType()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            BuildrDependencyFormatter.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        coordinates.setExtension("zip");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("org.carlspring.strongbox:maven-snippet:zip:1.0");
    }
}

