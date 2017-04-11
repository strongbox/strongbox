package org.carlspring.strongbox.locator.handlers;

import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.ArtifactFilenameFilter;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

/**
 * @author Kate Novik.
 */
public class MavenIndexerManagementOperation
        extends AbstractMavenArtifactLocatorOperation
{

    private static final Logger logger = LoggerFactory.getLogger(MavenIndexerManagementOperation.class);

    private RepositoryIndexManager repositoryIndexManager;


    public MavenIndexerManagementOperation(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }

    @Override
    public void executeOperation(VersionCollectionRequest request,
                                 String artifactPath,
                                 List<File> versionDirectories)
    {
        String repositoryId = getRepository().getId();
        String storageId = getStorage().getId();

        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);

        if (indexer != null)
        {
            for (File versionDirectory : versionDirectories)
            {
                // We're using System.out.println() here for clarity and due to the length of the lines
                // System.out.println(" " + versionDirectory.getAbsolutePath());

                String artifactVersionDirectory = versionDirectory.getPath();
                String artifactVersionDirectoryRelative = artifactVersionDirectory
                                                                  .substring(getStorage().getRepository(repositoryId)
                                                                                         .getBasedir()
                                                                                         .length() + 1,
                                                                             artifactVersionDirectory.length());

                String[] artifactCoordinateElements = artifactVersionDirectoryRelative.split("/");
                StringBuilder groupId = new StringBuilder();
                for (int i = 0; i < artifactCoordinateElements.length - 2; i++)
                {
                    String element = artifactCoordinateElements[i];
                    groupId.append((groupId.length() == 0) ? element : "." + element);
                }

                String artifactId = artifactCoordinateElements[artifactCoordinateElements.length - 2];
                String version = artifactCoordinateElements[artifactCoordinateElements.length - 1];

                DetachedArtifact artifact = (DetachedArtifact) ArtifactUtils.getArtifactFromGAVTC(groupId + ":" +
                                                                                                  artifactId + ":" +
                                                                                                  version);

                // TODO: @Sergey:
                // TODO: Could you please replace this with a fully Path-based implementation?
                File[] artifactFiles = versionDirectory.listFiles(new ArtifactFilenameFilter());
                if (artifactFiles != null)
                {
                    for (File artifactFile : artifactFiles)
                    {
                        String extension = artifactFile.getName().substring(artifactFile.getName().lastIndexOf('.') + 1,
                                                                            artifactFile.getName().length());

                        artifact.setFile(artifactFile);

                        // TODO: This is not quite right at the moment...
                        // TODO: SB-778: Figure out artifact extensions using Apache Tika
                        // TODO: Implement SB-778 here.
                        artifact.setType(extension);

                        try
                        {
                            indexer.addArtifactToIndex(repositoryId, artifactFile, artifact);
                        }
                        catch (IOException e)
                        {
                            logger.error("Failed to add artifact to index " + artifactPath + "!", e);
                        }
                    }
                }
            }
        }
    }

    public RepositoryIndexManager getRepositoryIndexManager()
    {
        return repositoryIndexManager;
    }

    public void setRepositoryIndexManager(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }

}
