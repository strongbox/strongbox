package org.carlspring.strongbox.services.impl;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.metadata.MetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

/**
 * @author stodorov
 */
@Component
public class ArtifactMetadataServiceImpl
        implements ArtifactMetadataService
{

    @Autowired
    private ConfigurationManager configurationManager;

    @Autowired
    private MetadataManager metadataManager;


    public ArtifactMetadataServiceImpl()
    {
    }

    @Override
    public Metadata getMetadata(String storageId,
                                String repositoryId,
                                Artifact artifact)
            throws IOException,
                   XmlPullParserException
    {
        Path artifactBasePath = artifact.getFile().toPath().getParent().getParent();

        return metadataManager.getMetadata(artifactBasePath);
    }

    @Override
    public Metadata getMetadata(String storageId,
                                String repositoryId,
                                String artifactPath)
            throws IOException,
                   XmlPullParserException
    {
        Path artifactBasePath = ArtifactUtils.convertPathToArtifact(artifactPath).getFile().toPath().getParent();
        return metadataManager.getMetadata(artifactBasePath);
    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Repository repository = configurationManager.getConfiguration().getStorage(storageId).getRepository(repositoryId);

        metadataManager.generateMetadata(repository, artifact);
    }

    public void rebuildMetadata(String storageId, String repositoryId, String artifactPath)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        rebuildMetadata(storageId, repositoryId, ArtifactUtils.convertPathToArtifact(artifactPath));
    }

    @Override
    public void mergeMetadata(String storageId,
                              String repositoryId,
                              Artifact artifact,
                              Metadata mergeMetadata)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        Repository repository = configurationManager.getConfiguration().getStorage(storageId).getRepository(repositoryId);

        metadataManager.mergeMetadata(repository, artifact, mergeMetadata);
    }

}
