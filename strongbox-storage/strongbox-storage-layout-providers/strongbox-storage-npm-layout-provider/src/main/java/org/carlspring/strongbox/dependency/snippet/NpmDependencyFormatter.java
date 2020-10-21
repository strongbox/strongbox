package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NpmDependencyFormatter
        implements DependencySynonymFormatter
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    public static final String ALIAS = "npm";

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
        return NpmLayoutProvider.ALIAS;
    }

    @Override
    public String getFormatAlias()
    {
        return ALIAS;
    }

    @Override
    public String getDependencySnippet(ArtifactCoordinates artifactCoordinates)
    {
        NpmArtifactCoordinates coordinates = (NpmArtifactCoordinates) artifactCoordinates;

        String sb = (coordinates.getScope() != null ? "\"" + coordinates.getScope() + "/" : "\"") +
                    "" + coordinates.getName() + "\" : " +
                    "\"" + coordinates.getVersion() + "\"\n";

        // TODO: Add support for scopes

        return sb;
    }

}
