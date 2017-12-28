package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponent;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class MavenMetadataGroupRepositoryComponent
        extends BaseMavenGroupRepositoryComponent
{

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Override
    protected void cleanupGroupWhenArtifactPathNoLongerExistsInSubTree(final Repository groupRepository,
                                                                       final String artifactPath)
            throws IOException
    {
        final LayoutProvider layoutProvider = getRepositoryProvider(groupRepository);
        layoutProvider.deleteMetadata(groupRepository.getStorage().getId(),
                                      groupRepository.getId(),
                                      artifactPath);
    }

    @Override
    protected UpdateCallback newInstance()
    {
        return new MetadataUpdateCallback();
    }

    class MetadataUpdateCallback
            implements UpdateCallback
    {

        private Metadata mergeMetadata;

        @Override
        public void beforeUpdate(final String storageId,
                                 final String repositoryId,
                                 final String artifactPath)
                throws IOException
        {
            final Repository repository = getRepository(storageId, repositoryId);
            final RepositoryPath repositoryAbsolutePath = getRepositoryPath(repository);
            final RepositoryPath artifactAbsolutePath = repositoryAbsolutePath.resolve(artifactPath);

            try
            {
                mergeMetadata = mavenMetadataManager.readMetadata(artifactAbsolutePath);
            }
            catch (final FileNotFoundException ex)
            {
                logger.warn("Unable to read metadata in repository path {}.", artifactAbsolutePath);
                throw new StopUpdateSilentlyException();
            }
            catch (final XmlPullParserException e)
            {
                throw new IOException(e);
            }
        }

        @Override
        public void performUpdate(final RepositoryPath parentRepositoryArtifactAbsolutePath)
                throws IOException
        {
            mavenMetadataManager.mergeAndStore(parentRepositoryArtifactAbsolutePath, mergeMetadata);
        }
    }

}
