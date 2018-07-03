package org.carlspring.strongbox.providers.layout;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.spi.FileSystemProvider;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public class NugetFileSystemProvider extends RepositoryLayoutFileSystemProvider
{

    private static final Logger logger = LoggerFactory.getLogger(NugetFileSystemProvider.class);

    @Inject
    private NugetLayoutProvider layoutProvider;

    public NugetFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super(storageFileSystemProvider);
    }

    @Override
    protected AbstractLayoutProvider getLayoutProvider()
    {
        return layoutProvider;
    }

    @Override
    protected ArtifactOutputStream decorateStream(RepositoryPath path,
                                                  OutputStream os,
                                                  ArtifactCoordinates artifactCoordinates)
        throws NoSuchAlgorithmException,
        IOException
    {
        ArtifactOutputStream result = super.decorateStream(path, os, artifactCoordinates);
        result.setDigestStringifier(layoutProvider::toBase64);
        return result;
    }
}
