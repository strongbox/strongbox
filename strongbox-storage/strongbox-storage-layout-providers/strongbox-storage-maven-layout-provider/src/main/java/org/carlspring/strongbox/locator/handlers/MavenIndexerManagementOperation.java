package org.carlspring.strongbox.locator.handlers;

import static org.carlspring.strongbox.util.IndexContextHelper.getContextId;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                                 RepositoryPath artifactPath,
                                 List<RepositoryPath> versionDirectories) throws IOException
    {
        String repositoryId = getRepository().getId();
        String storageId = getStorage().getId();

        String contextId = getContextId(storageId, repositoryId, IndexTypeEnum.LOCAL.getType());
        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndexer(contextId);

        if (indexer == null)
        {
            return;
        }
        for (RepositoryPath versionDirectoryAbs : versionDirectories)
        {
            // We're using System.out.println() here for clarity and due to the length of the lines
            // System.out.println(" " + versionDirectory.getAbsolutePath());

            RepositoryPath artifactVersionDirectoryRelative  = versionDirectoryAbs.getRepositoryRelative();
            int pathElementCount = artifactVersionDirectoryRelative.getNameCount();
            
            StringBuilder groupId = new StringBuilder();
            for (int i = 0; i < pathElementCount - 2; i++)
            {
                String element = artifactVersionDirectoryRelative.getName(i).toString();
                groupId.append((groupId.length() == 0) ? element : "." + element);
            }

            String artifactId = artifactVersionDirectoryRelative.getName(pathElementCount - 2).toString();
            String version = artifactVersionDirectoryRelative.getName(pathElementCount - 1).toString();           

            DetachedArtifact artifact = (DetachedArtifact) ArtifactUtils.getArtifactFromGAVTC(groupId + ":" +
                    artifactId + ":" +
                    version);

            Files.walk(versionDirectoryAbs)
                 .filter(Files::isRegularFile)
                 .forEach(f -> {
                     artifact.setFile(f.toFile());
                     try
                     {
                         indexer.addArtifactToIndex(repositoryId, f.toFile(), artifact);
                     }
                     catch (IOException e)
                     {
                         logger.error(String.format("Failed to add artifact to index for [%s]", f), e);
                     }
                 });

        }
    }

}
