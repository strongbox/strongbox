package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @Test
    public void testMavenDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            Maven2LayoutProvider.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "<dependency>\n" +
                     "    <groupId>" + coordinates.getGroupId() + "</groupId>\n" +
                     "    <artifactId>" + coordinates.getArtifactId() + "</artifactId>\n" +
                     "    <version>" + coordinates.getVersion() + "</version>\n" +
                     "    <type>" + coordinates.getExtension() + "</type>\n" +
                     "    <scope>compile</scope>\n" +
                     "</dependency>\n",
                     snippet);
    }


}
