package org.carlspring.strongbox.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.providers.storage.FileSystemStorageProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * This implementation defines an ArtifactFile path with {@link ArtifactCoordinates}. <br>
 * Against {@link RepositoryPath} it can be only a File (not a directory).
 * 
 * @author Sergey Bespalov
 */
public class ArtifactPath extends RepositoryPath
{
    private static final Logger logger = LoggerFactory.getLogger(ArtifactPath.class);

    private ArtifactCoordinates coordinates;

    public ArtifactPath(ArtifactCoordinates coordinates,
                        Path target,
                        RepositoryFileSystem fileSystem)
    {
        super(target, fileSystem);
        this.coordinates = coordinates;
    }

    public ArtifactCoordinates getCoordinates()
    {
        return coordinates;
    }

    public ArtifactFile toFile()
    {
        ArtifactCoordinates coordinatesLocal = getCoordinates();
        File source;
        try
        {
            source = getTarget().toFile();
        }
        catch (UnsupportedOperationException e)
        {
            logger.warn(String.format("toFile() unsupported: coordinates-[%s]; pathTarget-[%s]",
                                      coordinatesLocal,
                                      getTarget().getClass().getName()),
                        e);
            throw e;
        }
        ArtifactFile target = new ArtifactFile(getRoot().toString(), coordinatesLocal);
        try
        {
            Files.move(source, target);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to get File representation of ArtifactPath: coordinates-[%s]",
                                       coordinatesLocal));
            return null;
        }
        
        return target;
    }

    public static ArtifactPath getArtifactPath(Repository repository,
                                               ArtifactCoordinates coordinates)
        throws IOException
    {
        return new ArtifactPath(coordinates,
                                FileSystemStorageProvider.getArtifactPath(repository.getBasedir(),
                                                                          coordinates.toPath()),
                                RepositoryFileSystem.getRepositoryFileSystem(repository));
    } 
    
}
