package org.carlspring.strongbox.testing;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;

public class NullLayoutProvider extends AbstractLayoutProvider<NullArtifactCoordinates>
{

    @Inject
    private NullRepositoryManagementStrategy nullRepositoryManagementStragegy;

    @Override
    public RepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return nullRepositoryManagementStragegy;
    }

    @Override
    public String getAlias()
    {
        return NullArtifactCoordinates.LAYOUT_NAME;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return Collections.emptySet();
    }

    @Override
    protected boolean isArtifactMetadata(RepositoryPath repositoryPath)
    {
        return false;
    }

    @Override
    protected NullArtifactCoordinates getArtifactCoordinates(RepositoryPath repositoryPath)
        throws IOException
    {
        return new NullArtifactCoordinates(RepositoryFiles.relativizePath(repositoryPath));
    }

}
