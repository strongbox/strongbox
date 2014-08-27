package org.carlspring.strongbox.storage.services;

import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author stodorov
 */
public interface ArtifactMetadataService
{

    public Metadata getMetadata(String storageId, String repositoryId, Artifact artifact)
            throws IOException, XmlPullParserException;

    public void rebuildMetadata(String storageId, String repositoryId, Artifact artifact)
            throws IOException, XmlPullParserException;
}
