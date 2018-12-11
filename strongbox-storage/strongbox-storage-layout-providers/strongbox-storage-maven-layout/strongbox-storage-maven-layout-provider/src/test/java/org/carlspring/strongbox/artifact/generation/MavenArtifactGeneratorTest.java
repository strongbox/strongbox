package org.carlspring.strongbox.artifact.generation;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.MavenTestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenArtifactGeneratorTest
        extends MavenTestCaseWithArtifactGeneration
{


    @Test
    public void testArtifactGeneration()
            throws Exception
    {
        File basedir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.testing:matg:1.2.3:jar");

        generateArtifact(basedir.getAbsolutePath(), artifact);

        File artifactJarFile = new File(basedir, "org/carlspring/strongbox/testing/matg/1.2.3/matg-1.2.3.jar");
        File artifactJarFileMD5 = new File(basedir,
                                           "org/carlspring/strongbox/testing/matg/1.2.3/matg-1.2.3.jar.md5");
        File artifactJarFileSHA1 = new File(basedir,
                                            "org/carlspring/strongbox/testing/matg/1.2.3/matg-1.2.3.jar.sha1");

        File artifactPomFile = new File(basedir, "org/carlspring/strongbox/testing/matg/1.2.3/matg-1.2.3.pom");
        File artifactPomFileMD5 = new File(basedir,
                                           "org/carlspring/strongbox/testing/matg/1.2.3/matg-1.2.3.pom.md5");
        File artifactPomFileSHA1 = new File(basedir,
                                            "org/carlspring/strongbox/testing/matg/1.2.3/matg-1.2.3.pom.sha1");

        assertTrue(artifactJarFile.exists(), "Failed to generate JAR file!");
        assertTrue(artifactJarFileMD5.exists(), "Failed to generate JAR MD5 file!");
        assertTrue(artifactJarFileSHA1.exists(), "Failed to generate JAR SHA1 file!");

        assertTrue(artifactPomFile.exists(), "Failed to generate POM file!");
        assertTrue(artifactPomFileMD5.exists(), "Failed to generate POM MD5 file!");
        assertTrue(artifactPomFileSHA1.exists(), "Failed to generate POM SHA1 file!");

        String expectedJarMD5 = calculateChecksum(artifactJarFile, "MD5");
        String expectedJarSHA1 = calculateChecksum(artifactJarFile, "SHA1");

        String jarMd5 = MessageDigestUtils.readChecksumFile(artifactJarFileMD5.getAbsolutePath());
        String jarSha1 = MessageDigestUtils.readChecksumFile(artifactJarFileSHA1.getAbsolutePath());

        System.out.println("Expected  [MD5 ] (jar): " + expectedJarMD5);
        System.out.println("Generated [MD5 ] (jar): " + jarMd5);

        assertEquals(expectedJarMD5, jarMd5);

        System.out.println("Expected  [SHA1] (jar): " + expectedJarSHA1);
        System.out.println("Generated [SHA1] (jar): " + jarSha1);

        assertEquals(expectedJarSHA1, jarSha1);

        String expectedPomMD5 = calculateChecksum(artifactPomFile, "MD5");
        String expectedPomSHA1 = calculateChecksum(artifactPomFile, "SHA1");

        String pomMD5 = MessageDigestUtils.readChecksumFile(artifactPomFileMD5.getAbsolutePath());
        String pomSHA1 = MessageDigestUtils.readChecksumFile(artifactPomFileSHA1.getAbsolutePath());

        System.out.println("Expected  [MD5 ] (pom): " + expectedPomMD5);
        System.out.println("Generated [MD5 ] (pom): " + pomMD5);

        assertEquals(expectedPomMD5, pomMD5);

        System.out.println("Expected  [SHA1] (pom): " + expectedPomSHA1);
        System.out.println("Generated [SHA1] (pom): " + pomSHA1);

        assertEquals(expectedPomSHA1, pomSHA1);
    }

    private String calculateChecksum(File file,
                                     String type)
            throws Exception
    {
        byte[] buffer = new byte[4096];
        MessageDigest md = MessageDigest.getInstance(type);

        DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md);
        try
        {
            while (dis.read(buffer) != -1) ;
        }
        finally
        {
            dis.close();
        }

        return MessageDigestUtils.convertToHexadecimalString(md);
    }

}
