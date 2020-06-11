package org.carlspring.strongbox.testing;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;

public class NullLayoutProvider extends AbstractLayoutProvider<RawArtifactCoordinates>
{

    public static final String ALIAS = "Null Layout";
    
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
        return ALIAS;
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
    protected RawArtifactCoordinates getArtifactCoordinates(RepositoryPath repositoryPath)
        throws IOException
    {
        return new RawArtifactCoordinates(RepositoryFiles.relativizePath(repositoryPath));
    }

    protected Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.MD5)
                     .collect(Collectors.toSet());
    }

}
