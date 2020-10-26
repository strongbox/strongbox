package org.carlspring.strongbox.artifact.generator;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.IArtifactClient;
import org.carlspring.strongbox.storage.metadata.MetadataMerger;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.project.artifact.PluginArtifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class MavenArtifactDeployer
        extends MavenArtifactGenerator
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactDeployer.class);

    private String username;

    private String password;

    private IArtifactClient client;

    private MetadataMerger metadataMerger;

    public MavenArtifactDeployer(String basedir)
    {
        super(basedir);
    }

    public void generateAndDeployArtifact(Artifact artifact,
                                          String storageId,
                                          String repositoryId)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException, ArtifactTransportException
    {
        generateAndDeployArtifact(artifact, null, storageId, repositoryId, "jar");
    }

    public void generateAndDeployArtifact(Artifact artifact,
                                          String[] classifiers,
                                          String storageId,
                                          String repositoryId,
                                          String packaging)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException, ArtifactTransportException
    {
        generatePom(artifact, packaging);
        createArchive(artifact);

        deploy(artifact, storageId, repositoryId);
        deployPOM(MavenArtifactTestUtils.getPOMArtifact(artifact), storageId, repositoryId);

        if (classifiers != null)
        {
            for (String classifier : classifiers)
            {
                // We're assuming the type of the classifier is the same as the one of the main artifact
                Artifact artifactWithClassifier = MavenArtifactTestUtils.getArtifactFromGAVTC(artifact.getGroupId() + ":" +
                                                                                              artifact.getArtifactId() + ":" +
                                                                                              artifact.getVersion() + ":" +
                                                                                              artifact.getType() + ":" +
                                                                                              classifier);
                generate(artifactWithClassifier);

                deploy(artifactWithClassifier, storageId, repositoryId);
            }
        }

        mergeMetadata(artifact, storageId, repositoryId);
    }

    public void mergeMetadata(Artifact artifact,
                              String storageId,
                              String repositoryId)
            throws ArtifactTransportException,
                   IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException,
                   ArtifactOperationException
    {
        if (metadataMerger == null)
        {
            metadataMerger = new MetadataMerger();
        }

        Metadata metadata;
        if (ArtifactUtils.isSnapshot(artifact.getVersion()))
        {
            String path = MavenArtifactTestUtils.getVersionLevelMetadataPath(artifact);
            metadata = metadataMerger.updateMetadataAtVersionLevel(artifact,
                                                                   retrieveMetadata("/storages/" + storageId + "/" +
                                                                                    repositoryId + "/" + path));

            createMetadata(metadata, path);
            deployMetadata(metadata, path, storageId, repositoryId);
        }

        String path = MavenArtifactTestUtils.getArtifactLevelMetadataPath(artifact);
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact,
                                                                retrieveMetadata("/storages/" + storageId + "/" +
                                                                                 repositoryId + "/" + path));

        createMetadata(metadata, path);
        deployMetadata(metadata, path, storageId, repositoryId);

        if (artifact instanceof PluginArtifact)
        {
            path = MavenArtifactTestUtils.getGroupLevelMetadataPath(artifact);
            metadata = metadataMerger.updateMetadataAtGroupLevel((PluginArtifact) artifact,
                                                                 retrieveMetadata("/storages/" + storageId + "/" +
                                                                                  repositoryId + "/" + path));
            createMetadata(metadata, path);
            deployMetadata(metadata, path, storageId, repositoryId);
        }
    }

    private void deployMetadata(Metadata metadata,
                                String metadataPath,
                                String storageId,
                                String repositoryId)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactOperationException
    {
        File metadataFile = new File(getBasedir(), metadataPath);

        try (InputStream is = new FileInputStream(metadataFile);
             MultipleDigestInputStream mdis = new MultipleDigestInputStream(is))
        {
            String url = client.getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + metadataPath;

            logger.debug("Deploying {}...", url);

            client.deployMetadata(is, url, metadataPath.substring(metadataPath.lastIndexOf("/")));

            deployChecksum(mdis,
                           storageId,
                           repositoryId,
                           metadataPath.substring(0, metadataPath.lastIndexOf('/') + 1), "maven-metadata.xml");
        }
    }

    private void deployChecksum(MultipleDigestInputStream mdis,
                                String storageId,
                                String repositoryId,
                                String path,
                                String metadataFileName)
            throws ArtifactOperationException,
                   IOException
    {
        mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        mdis.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        for (Map.Entry entry : mdis.getHexDigests().entrySet())
        {
            final String algorithm = (String) entry.getKey();
            final String checksum = (String) entry.getValue();

            ByteArrayInputStream bais = new ByteArrayInputStream(checksum.getBytes());

            final String extensionForAlgorithm = EncryptionAlgorithmsEnum.fromAlgorithm(algorithm).getExtension();

            String artifactToPath = path + metadataFileName + extensionForAlgorithm;
            String url = client.getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/"
                         + artifactToPath;
            String artifactFileName = metadataFileName + extensionForAlgorithm;

            client.deployFile(bais, url, artifactFileName);
        }
    }

    public Metadata retrieveMetadata(String path)
            throws ArtifactTransportException,
                   IOException,
                   XmlPullParserException
    {
        if (client.pathExists(path))
        {
            try (InputStream is = client.getResource(path))
            {
                MetadataXpp3Reader reader = new MetadataXpp3Reader();

                return reader.read(is);
            }
        }

        return null;
    }

    public void deploy(Artifact artifact,
                       String storageId,
                       String repositoryId)
            throws ArtifactOperationException, IOException, NoSuchAlgorithmException
    {
        String artifactToPath = MavenArtifactUtils.convertArtifactToPath(artifact);
        File artifactFile = new File(getBasedir(), artifactToPath);
        try (InputStream is = new FileInputStream(artifactFile);
                MultipleDigestInputStream ais = new MultipleDigestInputStream(is))
        {
            String url = client.getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + artifactToPath;

            logger.debug("Deploying {}...", url);

            String fileName = MavenArtifactTestUtils.getArtifactFileName(artifact);

            client.deployFile(is, url, fileName);

            deployChecksum(ais, storageId, repositoryId, artifact);
        }
    }

    private void deployChecksum(MultipleDigestInputStream ais,
                                String storageId,
                                String repositoryId,
                                Artifact artifact)
            throws ArtifactOperationException, IOException
    {
        ais.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        ais.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        for (Map.Entry entry : ais.getHexDigests().entrySet())
        {
            final String algorithm = (String) entry.getKey();
            final String checksum = (String) entry.getValue();

            ByteArrayInputStream bais = new ByteArrayInputStream(checksum.getBytes());

            final String extensionForAlgorithm = EncryptionAlgorithmsEnum.fromAlgorithm(algorithm).getExtension();

            String artifactToPath = MavenArtifactUtils.convertArtifactToPath(artifact) + extensionForAlgorithm;
            String url =
                    client.getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + artifactToPath;
            String artifactFileName = MavenArtifactTestUtils.getArtifactFileName(artifact) + extensionForAlgorithm;

            client.deployFile(bais, url, artifactFileName);
        }
    }

    private void deployPOM(Artifact artifact,
                           String storageId,
                           String repositoryId)
            throws NoSuchAlgorithmException,
                   IOException,
                   ArtifactOperationException
    {
        String artifactToPath = MavenArtifactUtils.convertArtifactToPath(artifact);
        File pomFile = new File(getBasedir(), artifactToPath);

        try (InputStream is = new FileInputStream(pomFile);
                MultipleDigestInputStream ais = new MultipleDigestInputStream(is))
        {
            String url = client.getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + artifactToPath;

            String fileName = MavenArtifactTestUtils.getArtifactFileName(artifact);

            client.deployFile(is, url, fileName);

            deployChecksum(ais, storageId, repositoryId, artifact);
        }
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public IArtifactClient getClient()
    {
        return client;
    }

    public void setClient(IArtifactClient client)
    {
        this.client = client;
    }

}
