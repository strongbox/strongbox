package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.providers.ProviderImplementationException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        NpmArtifactCoordinates coordinates = new NpmArtifactCoordinates();
        coordinates.setId("angular");
        coordinates.setVersion("1.6.7");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("\"angular\" : \"1.6.7\"\n",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testNpmDependencyGenerationWithScope()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = new NpmDependencyFormatter();

        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        NpmArtifactCoordinates coordinates = new NpmArtifactCoordinates();
        coordinates.setId("angular");
        coordinates.setVersion("1.6.7");
        coordinates.setScope("@carlspring");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.print(snippet);

        assertEquals("\"@carlspring/angular\" : \"1.6.7\"\n",
                     snippet,
                     "Failed to generate dependency!");
    }

}
