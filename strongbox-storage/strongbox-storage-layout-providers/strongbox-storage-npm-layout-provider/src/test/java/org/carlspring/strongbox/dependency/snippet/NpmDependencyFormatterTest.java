package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
public class NpmDependencyFormatterTest
{


    @Test
    public void testNpmDependencyGenerationWithoutScope()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = new NpmDependencyFormatter();

        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        NpmArtifactCoordinates coordinates = new NpmArtifactCoordinates();
        coordinates.setId("angular");
        coordinates.setVersion("1.6.7");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("Failed to generate dependency!",
                     "\"angular\" : \"1.6.7\"\n",
                     snippet);
    }

    @Test
    public void testNpmDependencyGenerationWithScope()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = new NpmDependencyFormatter();

        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        NpmArtifactCoordinates coordinates = new NpmArtifactCoordinates();
        coordinates.setId("angular");
        coordinates.setVersion("1.6.7");
        coordinates.setScope("@carlspring");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("Failed to generate dependency!",
                     "\"@carlspring/angular\" : \"1.6.7\"\n",
                     snippet);
    }

}
