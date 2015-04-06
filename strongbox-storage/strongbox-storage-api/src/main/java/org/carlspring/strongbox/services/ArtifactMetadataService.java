package org.carlspring.strongbox.services;

import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author stodorov
 */
public interface ArtifactMetadataService
{

    Metadata getMetadata(String storageId, String repositoryId, Artifact artifact)
            throws IOException, XmlPullParserException;

    void rebuildMetadata(String storageId, String repositoryId, Artifact artifact)
            throws IOException, XmlPullParserException;

}
