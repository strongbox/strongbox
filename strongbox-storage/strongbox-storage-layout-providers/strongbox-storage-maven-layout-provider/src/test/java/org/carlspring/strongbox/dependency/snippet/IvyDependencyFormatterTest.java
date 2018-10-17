package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.inject.Inject;

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
public class IvyDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @Test
    public void testIvyDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            IvyDependencyFormatter.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "<dependency org=\"" + coordinates.getGroupId() + "\"" +
                     " name=\"" + coordinates.getArtifactId() + "\"" +
                     " rev=\"" + coordinates.getVersion() + "\" />\n",
                     snippet);
    }


}
