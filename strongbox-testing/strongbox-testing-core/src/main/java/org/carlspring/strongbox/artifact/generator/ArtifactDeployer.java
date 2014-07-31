package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactClient;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author mtodorov
 */
public class ArtifactDeployer extends ArtifactGenerator
{

    private String username;

    private String password;

    private ArtifactClient client;


    public ArtifactDeployer()
    {
    }

    public ArtifactDeployer(String basedir)
    {
        super(basedir);
    }

    public ArtifactDeployer(File basedir)
    {
        super(basedir);
    }

    public void initialize()
    {
        client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(48080);
        client.setContextBaseUrl("http://localhost:48080");
    }

    public void generateAndDeployArtifact(Artifact artifact,
                                          String storage,
                                          String repository)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException
    {
        initialize();

        generatePom(artifact);
        createArchive(artifact);

        deploy(artifact, storage, repository);
        deployPOM(ArtifactUtils.getPOMArtifact(artifact), storage, repository);

        // TODO: Update the metadata file on the repository's side.
    }

    public void deploy(Artifact artifact,
                       String storage,
                       String repository)
            throws ArtifactOperationException,
                   FileNotFoundException,
                   NoSuchAlgorithmException
    {
        File artifactFile = new File(getBasedir(), ArtifactUtils.convertArtifactToPath(artifact));
        ArtifactInputStream ais = new ArtifactInputStream(artifact, new FileInputStream(artifactFile));

        client.addArtifact(artifact, storage, repository, ais);

        deployChecksum(ais, storage, repository, artifact);
    }

    private void deployChecksum(ArtifactInputStream ais,
                                String storage,
                                String repository,
                                Artifact artifact)
            throws ArtifactOperationException
    {
        for (MessageDigest digest : ais.getDigests().values())
        {
            final String checksum = MessageDigestUtils.convertToHexadecimalString(digest);

            ByteArrayInputStream bais = new ByteArrayInputStream(checksum.getBytes());

            final String extensionForAlgorithm = MessageDigestUtils.getExtensionForAlgorithm(digest.getAlgorithm());

            String artifactToPath = ArtifactUtils.convertArtifactToPath(artifact) + extensionForAlgorithm;
            String url = client.getContextBaseUrl() + "/storages/" + storage + "/" + repository + "/" + artifactToPath;
            String artifactFileName = ais.getArtifactFileName() + extensionForAlgorithm;

            client.deployFile(bais, url, artifactFileName);
        }
    }

    private void deployPOM(Artifact artifact,
                           String storage,
                           String repository)
            throws NoSuchAlgorithmException,
                   FileNotFoundException,
                   ArtifactOperationException
    {
        File pomFile = new File(getBasedir(), ArtifactUtils.convertArtifactToPath(artifact));

        InputStream is = new FileInputStream(pomFile);
        ArtifactInputStream ais = new ArtifactInputStream(artifact, is);

        client.addArtifact(artifact, storage, repository, ais);

        deployChecksum(ais, storage, repository, artifact);
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

    public ArtifactClient getClient()
    {
        return client;
    }

    public void setClient(ArtifactClient client)
    {
        this.client = client;
    }

}
