package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.locator.handlers.GenerateMavenMetadataOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

/**
 * @author Pablo Tirado
 */
public class MavenMetadataServiceHelper
{

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected MavenMetadataManager mavenMetadataManager;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    private ConfigurationManager configurationManager;

    public void generateMavenMetadata(Repository repository)
            throws IOException
    {
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new GenerateMavenMetadataOperation(mavenMetadataManager,
                artifactEventListenerRegistry,
                getDigestAlgorithms(repository)));
        locator.locateArtifactDirectories();
    }

    private String[] getDigestAlgorithms(Repository repository) {
        MavenRepositoryConfiguration mavenRepositoryConfiguration = (MavenRepositoryConfiguration) repository.getRepositoryConfiguration();
        if (mavenRepositoryConfiguration != null) {
            Set<String> repoDigestAlgorithms = mavenRepositoryConfiguration.getDigestAlgorithmSet();
            if (repoDigestAlgorithms != null && repoDigestAlgorithms.size() > 0) {
                return repoDigestAlgorithms.toArray(new String[0]);
            }
        }
        return configurationManager.getConfiguration().getDigestAlgorithmSet().toArray(new String[0]);
    }
}
