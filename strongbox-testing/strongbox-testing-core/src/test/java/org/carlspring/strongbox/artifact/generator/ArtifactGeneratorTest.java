package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.maven.artifact.Artifact;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
public class ArtifactGeneratorTest
        extends TestCaseWithArtifactGeneration
{

    private final static File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");


    @Test
    public void testArtifactGeneration()
            throws Exception
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.testing:test-foo:1.2.3:jar");

        generateArtifact(BASEDIR.getAbsolutePath(), artifact);

        File artifactJarFile = new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.jar");
        File artifactJarFileMD5 = new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.jar.md5");
        File artifactJarFileSHA1 = new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.jar.sha1");

        File artifactPomFile = new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.pom");
        File artifactPomFileMD5 = new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.pom.md5");
        File artifactPomFileSHA1 = new File(BASEDIR, "org/carlspring/strongbox/testing/test-foo/1.2.3/test-foo-1.2.3.pom.sha1");

        assertTrue("Failed to generate JAR file!", artifactJarFile.exists());
        assertTrue("Failed to generate JAR MD5 file!", artifactJarFileMD5.exists());
        assertTrue("Failed to generate JAR SHA1 file!", artifactJarFileSHA1.exists());

        assertTrue("Failed to generate POM file!", artifactPomFile.exists());
        assertTrue("Failed to generate POM MD5 file!", artifactPomFileMD5.exists());
        assertTrue("Failed to generate POM SHA1 file!", artifactPomFileSHA1.exists());

        String expectedJarMD5 = calculateChecksum(artifactJarFile, "MD5");
        String expectedJarSHA1 = calculateChecksum(artifactJarFile, "SHA1");

        String jarMd5  = MessageDigestUtils.readChecksumFile(artifactJarFileMD5.getAbsolutePath());
        String jarSha1 = MessageDigestUtils.readChecksumFile(artifactJarFileSHA1.getAbsolutePath());

        System.out.println("Expected  [MD5 ] (jar): " + expectedJarMD5);
        System.out.println("Generated [MD5 ] (jar): " + jarMd5);

        assertEquals(expectedJarMD5, jarMd5);

        System.out.println("Expected  [SHA1] (jar): " + expectedJarSHA1);
        System.out.println("Generated [SHA1] (jar): " + jarSha1);

        assertEquals(expectedJarSHA1, jarSha1);

        String expectedPomMD5 = calculateChecksum(artifactPomFile, "MD5");
        String expectedPomSHA1 = calculateChecksum(artifactPomFile, "SHA1");

        String pomMD5  = MessageDigestUtils.readChecksumFile(artifactPomFileMD5.getAbsolutePath());
        String pomSHA1 = MessageDigestUtils.readChecksumFile(artifactPomFileSHA1.getAbsolutePath());

        System.out.println("Expected  [MD5 ] (pom): " + expectedPomMD5);
        System.out.println("Generated [MD5 ] (pom): " + pomMD5);

        assertEquals(expectedPomMD5, pomMD5);

        System.out.println("Expected  [SHA1] (pom): " + expectedPomSHA1);
        System.out.println("Generated [SHA1] (pom): " + pomSHA1);

        assertEquals(expectedPomSHA1, pomSHA1);
    }

    private String calculateChecksum(File file, String type) throws Exception
    {
        byte[] buffer = new byte[4096];
        MessageDigest md = MessageDigest.getInstance(type);

        DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);
        try {
            while(dis.read(buffer) != -1);
        }finally{
            dis.close();
        }

        return MessageDigestUtils.convertToHexadecimalString(md);
    }

}