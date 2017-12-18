package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.config.NugetLayoutProviderConfig;
import org.carlspring.strongbox.config.NugetLayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;

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
@ContextConfiguration(classes = NugetLayoutProviderTestConfig.class)
public class NugetDependencyFormatterTest
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @Test
    public void testGradleDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(NugetLayoutProvider.ALIAS,
                                                                                                            NugetDependencyFormatter.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        NugetArtifactCoordinates coordinates = new NugetArtifactCoordinates();
        coordinates.setId("Org.Carlspring.Strongbox.NuGet.Snippet");
        coordinates.setVersion("1.0");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "<dependency id=\"" + coordinates.getId() + "\" version=\"" + coordinates.getVersion() + "\" />\n",
                     snippet);
    }

}
