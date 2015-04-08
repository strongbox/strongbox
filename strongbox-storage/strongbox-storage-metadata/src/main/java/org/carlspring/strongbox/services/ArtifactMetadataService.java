package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author stodorov
 */
public interface ArtifactMetadataService
{

    /**
     * Get artifact metadata.
     *
     * @param storageId     String
     * @param repositoryId  String
     * @param artifact      Artifact
     *
     * @return Metadata
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public Metadata getMetadata(String storageId, String repositoryId, Artifact artifact)
            throws IOException, XmlPullParserException;

    /**
     * Get artifact metadata using artifactPath(string) instead of Artifact.
     *
     * @param storageId     String
     * @param repositoryId  String
     * @param artifactPath  String
     *
     * @return Metadata
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public Metadata getMetadata(String storageId, String repositoryId, String artifactPath)
            throws IOException, XmlPullParserException;

    /**
     * Rebuild metadata for artifact
     *
     * @param storageId     String
     * @param repositoryId  String
     * @param artifact      Artifact
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public void rebuildMetadata(String storageId, String repositoryId, Artifact artifact)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException;

    /**
     * Rebuild metadata for artifact using artifactPath (string)
     *
     * @param storageId     String
     * @param repositoryId  String
     * @param artifactPath  String
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public void rebuildMetadata(String storageId, String repositoryId, String artifactPath)
            throws IOException, XmlPullParserException, NoSuchAlgorithmException;

    /**
     * Merge existing artifact metadata with mergeMetadata.
     *
     * @param storageId     String
     * @param repositoryId  String
     * @param artifact      Artifact
     * @param mergeMetadata Metadata
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public void mergeMetadata(String storageId, String repositoryId, Artifact artifact, Metadata mergeMetadata)
            throws IOException, XmlPullParserException;

}
