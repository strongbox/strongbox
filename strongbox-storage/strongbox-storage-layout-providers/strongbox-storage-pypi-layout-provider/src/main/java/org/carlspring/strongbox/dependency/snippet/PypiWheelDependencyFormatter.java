package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.PypiWheelArtifactCoordinates;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.providers.layout.PypiLayoutProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
* This class is an implementation of DependencySynonymFormatter for Pypi artifacts
*
* @author whalenda
**/
@Component
public class PypiWheelDependencyFormatter
    implements DependencySynonymFormatter
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractLayoutProvider.class);

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;
    @PostConstruct
    @Override
    public void register()
    {
        compatibleDependencyFormatRegistry.addProviderImplementation(getLayout(), getFormatAlias(), this);
        logger.debug("Initialized the Pypi dependency formatter.");
    }

    @Override
    public String getLayout()
    {
        return PypiLayoutProvider.ALIAS;
    }

    @Override
    public String getFormatAlias()
    {
        return PypiLayoutProvider.ALIAS;
    }

    /**
    * This method takes in a set of Pypi Artifact Coordinates and returns the properly formatted dependency snippet
    * @param a PyPiWheelArtifactCoordinates object
    * @return a string representing a properly formatted dependency snippet
    **/
    @Override
    public String getDependencySnippet(ArtifactCoordinates inputCoordinates)
    {
        PypiWheelArtifactCoordinates coordinates = (PypiWheelArtifactCoordinates) inputCoordinates;
        String sb = coordinates.getId(); 

        if (!"version".equals(coordinates.getVersion()))
        {
            sb += " == " + coordinates.getVersion();
        }
        return sb;
    }
}
