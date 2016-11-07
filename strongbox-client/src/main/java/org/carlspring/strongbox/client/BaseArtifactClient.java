package org.carlspring.strongbox.client;

import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements basic API for artifact processing. Subclasses may specify particular remote method implementations.
 *
 * @author Alex Oreshkevich
 */
public abstract class BaseArtifactClient
        implements IArtifactClient
{

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public void addArtifact(Artifact artifact,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Deploying " + url + "...");

        String fileName = ArtifactUtils.getArtifactFileName(artifact);

        deployFile(is, url, fileName);
    }

    public void addMetadata(Metadata metadata,
                            String path,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path;

        logger.debug("Deploying " + url + "...");

        deployMetadata(is, url, path.substring(path.lastIndexOf("/")));
    }

    public InputStream getResource(String path)
            throws ArtifactTransportException,
                   IOException
    {
        return getResource(path, 0);
    }

    protected String escapeUrl(String path)
    {
        String baseUrl = getContextBaseUrl() + (getContextBaseUrl().endsWith("/") ? "" : "/");
        String p = (path.startsWith("/") ? path.substring(1, path.length()) : path);

        return baseUrl + p;
    }

    public Metadata retrieveMetadata(String path)
            throws ArtifactTransportException, IOException, XmlPullParserException
    {
        if (pathExists(path))
        {
            InputStream is = getResource(path);
            try
            {
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                return reader.read(is);
            }
            finally
            {
                is.close();
            }
        }
        return null;
    }

    protected abstract void put(InputStream is,
                                String url,
                                String fileName,
                                String mediaType)
            throws ArtifactOperationException;
}
