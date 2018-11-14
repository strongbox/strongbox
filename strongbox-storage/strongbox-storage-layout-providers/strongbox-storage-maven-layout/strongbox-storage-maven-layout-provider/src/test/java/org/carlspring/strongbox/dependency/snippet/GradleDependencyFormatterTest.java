package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 */
@ExtendWith(SpringExtension.class)
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
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("compile \"org.carlspring.strongbox:maven-snippet:1.0\"\n",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testGradleDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            GradleDependencyFormatter.ALIAS);
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        // compile "org.gradle.test.classifiers:service:1.0:jdk15@jar"
        assertEquals("compile \"org.carlspring.strongbox:maven-snippet:1.0:jdk12\"\n",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testGradleDependencyGenerationWithExtensionAndClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            GradleDependencyFormatter.ALIAS);
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("zip");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        // compile "org.gradle.test.classifiers:service:1.0:jdk15@jar"
        assertEquals("compile \"org.carlspring.strongbox:maven-snippet:1.0:jdk12@zip\"\n",
                     snippet,
                     "Failed to generate dependency!");
    }

}
