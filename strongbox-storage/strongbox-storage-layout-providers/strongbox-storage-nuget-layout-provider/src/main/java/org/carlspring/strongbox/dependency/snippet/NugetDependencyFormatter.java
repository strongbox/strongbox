package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NugetArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NugetDependencyFormatter
        implements DependencySynonymFormatter
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    public static final String ALIAS = "NuGet";

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;


    @PostConstruct
    @Override
    public void register()
    {
        compatibleDependencyFormatRegistry.addProviderImplementation(getLayout(), getFormatAlias(), this);

        logger.debug("Initialized the NuGet dependency formatter.");
    }

    @Override
    public String getLayout()
    {
        return NugetLayoutProvider.ALIAS;
    }

    @Override
    public String getFormatAlias()
    {
        return ALIAS;
    }

    @Override
    public String getDependencySnippet(ArtifactCoordinates artifactCoordinates)
    {
        NugetArtifactCoordinates coordinates = (NugetArtifactCoordinates) artifactCoordinates;

        String sb = "<dependency id=\"" + coordinates.getId() + "\"" +
                    " version=\"" + coordinates.getVersion() + "\" />\n";

        return sb;
    }

}
