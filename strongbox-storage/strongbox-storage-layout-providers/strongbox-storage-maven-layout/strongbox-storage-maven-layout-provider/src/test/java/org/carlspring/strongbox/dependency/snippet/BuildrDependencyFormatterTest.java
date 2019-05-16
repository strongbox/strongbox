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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

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
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("org.carlspring.strongbox:maven-snippet:jar:1.0",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testBuildrDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            BuildrDependencyFormatter.ALIAS);
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        coordinates.setExtension("jar");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("org.carlspring.strongbox:maven-snippet:jar:jdk12:1.0",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testBuildrDependencyGenerationWithNonStandardType()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            BuildrDependencyFormatter.ALIAS);
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        coordinates.setExtension("zip");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("org.carlspring.strongbox:maven-snippet:zip:1.0",
                     snippet,
                     "Failed to generate dependency!");
    }
}

