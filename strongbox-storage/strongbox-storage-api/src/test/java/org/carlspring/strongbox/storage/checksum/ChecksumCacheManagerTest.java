package org.carlspring.strongbox.storage.checksum;

import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Ignore;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * @author mtodorov
 */
@Ignore
public class ChecksumCacheManagerTest
{


    @Test
    public void testChecksumManagement()
            throws Exception
    {
        ChecksumCacheManager manager = new ChecksumCacheManager();
        manager.setCachedChecksumLifetime(3000l);
        manager.setCachedChecksumExpiredCheckInterval(500l);
        manager.startMonitor();

        final String artifact1BasePath = "storage0/repositories/snapshots/org/carlspring/maven/test-project/1.0-SNAPSHOT/maven-metadata.xml";
        final String artifact2BasePath = "storage0/repositories/snapshots/org/carlspring/maven/test-project/1.0-SNAPSHOT/test-project-1.0-20131004.115330-1.jar";

        manager.addArtifactChecksum(artifact1BasePath, "md5", "d0s#3E59jszLsPj3#edp!$");
        manager.addArtifactChecksum(artifact1BasePath, "sha1", "d0s#3E59jszLsPj3#edp!$");
        manager.addArtifactChecksum(artifact2BasePath, "md5", "eps0#!_)fs0-qWadg#)s1!");
        manager.addArtifactChecksum(artifact2BasePath, "sha1", "eps0#!_)fs0-qWadg#)s1!");

        Thread.sleep(3000l);

        manager.getArtifactChecksum(artifact1BasePath, "md5");

        Thread.sleep(3000l);

        assertEquals("Failed to expire checksums from cache!", 1, manager.getSize());
    }

    @Test
    public void testDigests()
            throws NoSuchAlgorithmException, IOException, CloneNotSupportedException
    {
        String s = "This is a test.";

        MessageDigest md5Digest = MessageDigest.getInstance("MD5");
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");

        md5Digest.update(s.getBytes());
        sha1Digest.update(s.getBytes());

        String md5 = MessageDigestUtils.convertToHexadecimalString(md5Digest);
        String sha1 = MessageDigestUtils.convertToHexadecimalString(sha1Digest);

        assertEquals("Incorrect MD5 sum!", "120ea8a25e5d487bf68b5f7096440019", md5);
        assertEquals("Incorrect SHA-1 sum!", "afa6c8b3a2fae95785dc7d9685a57835d703ac88", sha1);

        System.out.println("md5:  " + md5);
        System.out.println("sha1: " + sha1);
    }

}
