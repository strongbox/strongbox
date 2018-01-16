package org.carlspring.strongbox.locator.handlers;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class MavenIndexerManagementOperation
        extends AbstractMavenArtifactLocatorOperation
{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected ArtifactIndexesService artifactIndexesService;

    public MavenIndexerManagementOperation(ArtifactIndexesService artifactIndexesService)
    {
        this.artifactIndexesService = artifactIndexesService;
    }

    @Override
    public void executeOperation(VersionCollectionRequest request,
                                 RepositoryPath artifactPath,
                                 List<RepositoryPath> versionDirectories)
            throws IOException
    {
        for (RepositoryPath versionDirectoryAbs : versionDirectories)
        {
            try (Stream<Path> pathStream = Files.walk(versionDirectoryAbs))
            {
                pathStream.filter(Files::isRegularFile)
                          .forEach(filePath ->
                                   {
                                       try
                                       {

                                           RepositoryIndexer repositoryIndexer = getRepositoryIndexer();
                                           if (repositoryIndexer != null)
                                           {
                                               artifactIndexesService.addArtifactToIndex((RepositoryPath) filePath,
                                                                                         repositoryIndexer);
                                           }
                                           else
                                           {
                                               artifactIndexesService.addArtifactToIndex((RepositoryPath) filePath);
                                           }
                                       }
                                       catch (IOException e)
                                       {
                                           logger.error(
                                                   String.format("Failed to add artifact to index for [%s]", filePath),
                                                   e);
                                       }
                                   });
            }
        }
    }

    protected RepositoryIndexer getRepositoryIndexer()
    {
        return null;
    }

}
