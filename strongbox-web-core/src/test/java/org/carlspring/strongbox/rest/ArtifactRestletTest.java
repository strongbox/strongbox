package org.carlspring.strongbox.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.artifact.PluginArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mtodorov
 */
public class ArtifactRestletTest
        extends TestCaseWithArtifactGeneration
{

    private static final File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/local");

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    public static boolean INITIALIZED = false;

    private RestClient client = new RestClient();


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            // Generate releases
            // Used by testPartialFetch():
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                             "org.carlspring.strongbox.partial:partial-foo",
                             new String[]{ "3.1", // Used by testPartialFetch()
                                           "3.2"  // Used by testPartialFetch()
                                         });

            // Used by testCopy*():
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                             "org.carlspring.strongbox.copy:copy-foo",
                             new String[]{ "1.1", // Used by testCopyArtifactFile()
                                           "1.2"  // Used by testCopyArtifactDirectory()
                                         });

            // Used by testDelete():
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                             "com.artifacts.to.delete.releases:delete-foo",
                             new String[]{ "1.2.1", // Used by testDeleteArtifactFile
                                           "1.2.2"  // Used by testDeleteArtifactDirectory
                                         });
                
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(),
                             "org.carlspring.strongbox.partial:partial-foo",
                             new String[]{ "3.1", // Used by testPartialFetch()
                                           "3.2"  // Used by testPartialFetch()
                                         });

            INITIALIZED = true;
        }
    }

    @After
    public void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public void testPartialFetch()
            throws Exception
    {
        String artifactPath = "/storages/storage0/releases/org/carlspring/strongbox/partial/partial-foo/3.1/partial-foo-3.1.jar";

        assertTrue("Artifact does not exist!", client.pathExists(artifactPath));

        String md5Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".md5"));
        String sha1Remote = MessageDigestUtils.readChecksumFile(client.getResource(artifactPath + ".sha1"));

        InputStream is = client.getResource(artifactPath);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);

        int size = 1024;
        byte[] bytes = new byte[size];
        int total = 0;
        int len;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            total += len;
            if (total >= size)
            {
                break;
            }
        }

        mdos.flush();

        bytes = new byte[size];
        is.close();

        System.out.println("Read " + total + " bytes.");

        is = client.getResource(artifactPath, total);

        System.out.println("Skipped " + total + " bytes.");

        int partialRead = total;
        int len2 = 0;

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            mdos.write(bytes, 0, len);

            len2 += len;
            total += len;
        }

        mdos.flush();

        System.out.println("Wrote " + total + " bytes.");
        System.out.println("Partial read, terminated after writing " + partialRead + " bytes.");
        System.out.println("Partial read, continued and wrote " + len2 + " bytes.");
        System.out.println("Partial reads: total written bytes: " + (partialRead + len2) + ".");

        mdos.close();

        final String md5Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        final String sha1Local = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        System.out.println("MD5   [Remote]: " + md5Remote);
        System.out.println("MD5   [Local ]: " + md5Local);

        System.out.println("SHA-1 [Remote]: " + sha1Remote);
        System.out.println("SHA-1 [Local ]: " + sha1Local);

        FileOutputStream output = new FileOutputStream(new File("target/partial-foo-3.1.jar"));
        output.write(baos.toByteArray());

        assertEquals("Glued partial fetches did not match MD5 checksum!", md5Remote, md5Local);
        assertEquals("Glued partial fetches did not match SHA-1 checksum!", sha1Remote, sha1Local);
        output.close();
    }

    @Test
    public void testCopyArtifactFile()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.1/copy-foo-1.1.jar";

        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();

        assertFalse("Unexpected artifact in repository '" + destRepositoryBasedir + "'!",
                    artifactFileRestoredFromTrash.exists());

        client.copy(artifactPath,
                    "storage0",
                    "releases",
                    "storage0",
                    "releases-with-trash");

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testCopyArtifactDirectory()
            throws Exception
    {
        final File destRepositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                    "/storages/storage0/releases-with-trash");

        String artifactPath = "org/carlspring/strongbox/copy/copy-foo/1.2";

        File artifactFileRestoredFromTrash = new File(destRepositoryBasedir + "/" + artifactPath).getAbsoluteFile();

        assertFalse("Unexpected artifact in repository '" + destRepositoryBasedir + "'!",
                    artifactFileRestoredFromTrash.exists());

        client.copy(artifactPath,
                    "storage0",
                    "releases",
                    "storage0",
                    "releases-with-trash");

        assertTrue("Failed to copy artifact to destination repository '" + destRepositoryBasedir + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testDeleteArtifactFile()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.1/delete-foo-1.2.1.jar";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" + artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());

        client.delete("storage0", "releases", artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());
    }

    @Test
    public void testDeleteArtifactDirectory()
            throws Exception
    {
        String artifactPath = "com/artifacts/to/delete/releases/delete-foo/1.2.2";

        File deletedArtifact = new File(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath() + "/" + artifactPath).getAbsoluteFile();

        assertTrue("Failed to locate artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());

        client.delete("storage0", "releases", artifactPath);

        assertFalse("Failed to delete artifact file '" + deletedArtifact.getAbsolutePath() + "'!", deletedArtifact.exists());
    }

    @Test
    public void testMetadataAtVersionLevel()
            throws NoSuchAlgorithmException,
                   ArtifactOperationException,
                   IOException,
                   XmlPullParserException,
                   ArtifactTransportException
    {
        String ga = "org.carlspring.strongbox.metadata:metadata-foo";

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC(ga + ":3.1-SNAPSHOT");
        Artifact artifact1WithTimestamp1 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 1));
        Artifact artifact1WithTimestamp2 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 2));
        Artifact artifact1WithTimestamp3 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 3));
        Artifact artifact1WithTimestamp4 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 4));

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(GENERATOR_BASEDIR);
        artifactDeployer.setClient(client);
        
        String storageId = "storage0";
        String repositoryId = "snapshots";

        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp1, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp2, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp3, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp4, storageId, repositoryId);
        
        Metadata versionLevelMetadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                                                         ArtifactUtils.getVersionLevelMetadataPath(artifact1));
        
        Assert.assertNotNull(versionLevelMetadata);
        Assert.assertEquals("org.carlspring.strongbox.metadata", versionLevelMetadata.getGroupId());
        Assert.assertEquals("metadata-foo", versionLevelMetadata.getArtifactId());
        Assert.assertEquals(4, versionLevelMetadata.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertNotNull(versionLevelMetadata.getVersioning().getLastUpdated());
        Assert.assertEquals(12, versionLevelMetadata.getVersioning().getSnapshotVersions().size()    );
    }

    @Test
    public void testMetadataAtGroupAndArtifactIdLevel()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   ArtifactTransportException
    {
        // Given
        // Plugin Artifacts
        String groupId = "org.carlspring.strongbox.metadata";
        String artifactId1 = "metadata-foo-plugin";
        String artifactId2 = "metadata-faa-plugin";
        String artifactId3 = "metadata-foo";
        String version1 = "3.1";
        String version2 = "3.2";
        
        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId1+":"+ version1);
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId2+":"+ version1);
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId1+":"+ version2);
        Artifact artifact4 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId2+":"+ version2);

        // Artifacts
        Artifact artifact5 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId3+":"+ version1);
        Artifact artifact6 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId3+":"+ version2);

        Plugin p1 = new Plugin();
        p1.setGroupId(artifact1.getGroupId());
        p1.setArtifactId(artifact1.getArtifactId());
        p1.setVersion(artifact1.getVersion());

        Plugin p2 = new Plugin();
        p2.setGroupId(artifact2.getGroupId());
        p2.setArtifactId(artifact2.getArtifactId());
        p2.setVersion(artifact2.getVersion());

        Plugin p3 = new Plugin();
        p3.setGroupId(artifact3.getGroupId());
        p3.setArtifactId(artifact3.getArtifactId());
        p3.setVersion(artifact3.getVersion());

        Plugin p4 = new Plugin();
        p4.setGroupId(artifact4.getGroupId());
        p4.setArtifactId(artifact4.getArtifactId());
        p4.setVersion(artifact4.getVersion());

        PluginArtifact a = new PluginArtifact(p1, artifact1);
        PluginArtifact b = new PluginArtifact(p2, artifact2);
        PluginArtifact c = new PluginArtifact(p3, artifact3);
        PluginArtifact d = new PluginArtifact(p4, artifact4);

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(GENERATOR_BASEDIR);
        artifactDeployer.setClient(client);

        String storageId = "storage0";
        String repositoryId = "releases";

        // When
        artifactDeployer.generateAndDeployArtifact(a, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(b, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(c, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(d, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact5, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact6, storageId, repositoryId);

        // Then
        // Group level metadata
        Metadata groupLevelMetadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                                                       ArtifactUtils.getGroupLevelMetadataPath(artifact1));

        Assert.assertNotNull(groupLevelMetadata);
        Assert.assertEquals(2, groupLevelMetadata.getPlugins().size());

        // Artifact Level metadata
        Metadata artifactLevelMetadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                                                          ArtifactUtils.getArtifactLevelMetadataPath(artifact1));

        Assert.assertNotNull(artifactLevelMetadata);
        Assert.assertEquals(groupId, artifactLevelMetadata.getGroupId());
        Assert.assertEquals(artifactId1, artifactLevelMetadata.getArtifactId());
        Assert.assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        Assert.assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        Assert.assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        Assert.assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());

        artifactLevelMetadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                                                 ArtifactUtils.getArtifactLevelMetadataPath(artifact2));

        Assert.assertNotNull(artifactLevelMetadata);
        Assert.assertEquals(groupId, artifactLevelMetadata.getGroupId());
        Assert.assertEquals(artifactId2, artifactLevelMetadata.getArtifactId());
        Assert.assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        Assert.assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        Assert.assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        Assert.assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());

        artifactLevelMetadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                                                 ArtifactUtils.getArtifactLevelMetadataPath(artifact5));

        Assert.assertNotNull(artifactLevelMetadata);
        Assert.assertEquals(groupId, artifactLevelMetadata.getGroupId());
        Assert.assertEquals(artifactId3, artifactLevelMetadata.getArtifactId());
        Assert.assertEquals(version2, artifactLevelMetadata.getVersioning().getLatest());
        Assert.assertEquals(version2, artifactLevelMetadata.getVersioning().getRelease());
        Assert.assertEquals(2, artifactLevelMetadata.getVersioning().getVersions().size());
        Assert.assertNotNull(artifactLevelMetadata.getVersioning().getLastUpdated());
    }
    
    @Test
    public void updateMetadataOndeleteReleaseVersionDirectoryTest() throws NoSuchAlgorithmException, XmlPullParserException, IOException, ArtifactOperationException, ArtifactTransportException 
    {
        // Given
        // Plugin Artifacts
        String groupId = "org.carlspring.strongbox.metadata";
        String artifactId = "metadata-foo";
        String version1 = "3.1";
        String version2 = "3.2";
        
        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId+":"+ version1);
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC(groupId+":"+artifactId+":"+ version2);
        
        ArtifactDeployer artifactDeployer = new ArtifactDeployer(GENERATOR_BASEDIR);
        artifactDeployer.setClient(client);

        String storageId = "storage0";
        String repositoryId = "releases";


        artifactDeployer.generateAndDeployArtifact(artifact1, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact2, storageId, repositoryId);

        // When
        String path = "org/carlspring/strongbox/metadata/metadata-foo/3.2";
        client.delete(storageId, repositoryId, path);
        
        Metadata metadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                ArtifactUtils.getArtifactLevelMetadataPath(artifact1));
        Assert.assertTrue(!metadata.getVersioning().getVersions().contains("3.2"));
    }
    
    @Test 
    public void updateMetadataOnDeleteSnapshotVersionDirectoryTest() throws NoSuchAlgorithmException, XmlPullParserException, IOException, ArtifactOperationException, ArtifactTransportException 
    {
        //Given
        String ga = "org.carlspring.strongbox.metadata:metadata-foo";

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC(ga + ":3.1-SNAPSHOT");
        Artifact artifact1WithTimestamp1 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 1));
        Artifact artifact1WithTimestamp2 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 2));
        Artifact artifact1WithTimestamp3 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 3));
        Artifact artifact1WithTimestamp4 = ArtifactUtils.getArtifactFromGAVTC(ga + ":" + createSnapshotVersion("3.1", 4));

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(GENERATOR_BASEDIR);
        artifactDeployer.setClient(client);
        
        String storageId = "storage0";
        String repositoryId = "snapshots";

        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp1, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp2, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp3, storageId, repositoryId);
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp4, storageId, repositoryId);
        
        String path = "org/carlspring/strongbox/metadata/metadata-foo/3.1-SNAPSHOT";
        
        //When
        client.delete(storageId, repositoryId, path);
            
        //Then
        Metadata metadata = client.retrieveMetadata("storages/" + storageId + "/" + repositoryId + "/" +
                ArtifactUtils.getArtifactLevelMetadataPath(artifact1));
        Assert.assertTrue(!metadata.getVersioning().getVersions().contains("3.1-SNAPSHOT"));
    }
}
