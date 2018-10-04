package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class LeiningenDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    MavenArtifactCoordinates coordinates;

    @Before
    public void setUp()
            throws Exception
    {
        coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
    }

    @Test
    public void testIvyDependencyGenerationWithoutClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            LeiningenDependencyFormatter.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "[org.carlspring.strongbox/maven-snippet \"1.0\"]\n",
                     snippet);
    }

    @Test
    public void testIvyDependencyGenerationWithNonStandardType()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            LeiningenDependencyFormatter.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        coordinates.setExtension("zip");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "[org.carlspring.strongbox/maven-snippet \"1.0\" :extension \"zip\"]\n",
                     snippet);
    }

    @Test
    public void testIvyDependencyGenerationWithNonStandardTypeAndClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            LeiningenDependencyFormatter.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        coordinates.setExtension("zip");
        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "[org.carlspring.strongbox/maven-snippet \"1.0\" :extension \"zip\" :classifier \"jdk12\"]\n",
                     snippet);
    }

    @Test
    public void testIvyDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            LeiningenDependencyFormatter.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        coordinates.setClassifier("jdk12");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "[org.carlspring.strongbox/maven-snippet \"1.0\" :classifier \"jdk12\"]\n",
                     snippet);
    }


}
