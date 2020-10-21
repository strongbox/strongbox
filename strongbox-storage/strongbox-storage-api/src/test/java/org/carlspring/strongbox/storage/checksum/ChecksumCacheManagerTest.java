package org.carlspring.strongbox.storage.checksum;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.util.MessageDigestUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mtodorov
 */
public class ChecksumCacheManagerTest
{


    @Test
    public void testChecksumManagement()
            throws Exception
    {
        ChecksumCacheManager manager = new ChecksumCacheManager();
        manager.setCachedChecksumLifetime(3000L);
        manager.setCachedChecksumExpiredCheckInterval(500L);

        CheckingThread checkerThread = new CheckingThread(manager);

        final String artifact1BasePath = "storage0/repositories/snapshots/org/carlspring/maven/test-project/1.0-SNAPSHOT/maven-metadata.xml";
        final String artifact2BasePath = "storage0/repositories/snapshots/org/carlspring/maven/test-project/1.0-SNAPSHOT/test-project-1.0-20131004.115330-1.jar";

        manager.addArtifactChecksum(artifact1BasePath, "md5", "d0s#3E59jszLsPj3#edp!$");
        manager.addArtifactChecksum(artifact1BasePath, "sha1", "d0s#3E59jszLsPj3#edp!$");
        manager.addArtifactChecksum(artifact2BasePath, "md5", "eps0#!_)fs0-qWadg#)s1!");
        manager.addArtifactChecksum(artifact2BasePath, "sha1", "eps0#!_)fs0-qWadg#)s1!");

        manager.startMonitor();

        checkerThread.start();

        Thread.sleep(3000L);

        manager.getArtifactChecksum(artifact1BasePath, "md5");

        System.out.println("Slept " + checkerThread.getTimeSlept() + " ms");

        if (checkerThread.getTimeSlept() > (checkerThread.getMaxTime() + checkerThread.getTolerance()))
        {
            fail("Failed to expire the cache on time!");
        }

        checkerThread.interrupt();
    }

    @Test
    public void testDigests()
            throws NoSuchAlgorithmException, IOException, CloneNotSupportedException
    {
        String s = "This is a test.";

        MessageDigest md5Digest = MessageDigest.getInstance(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
        MessageDigest sha1Digest = MessageDigest.getInstance(EncryptionAlgorithmsEnum.SHA1.getAlgorithm());

        md5Digest.update(s.getBytes());
        sha1Digest.update(s.getBytes());

        String md5 = MessageDigestUtils.convertToHexadecimalString(md5Digest);
        String sha1 = MessageDigestUtils.convertToHexadecimalString(sha1Digest);

        assertThat(md5).as("Incorrect MD5 sum!").isEqualTo("120ea8a25e5d487bf68b5f7096440019");
        assertThat(sha1).as("Incorrect SHA-1 sum!").isEqualTo("afa6c8b3a2fae95785dc7d9685a57835d703ac88");

        System.out.println("md5:  " + md5);
        System.out.println("sha1: " + sha1);
    }

    private class CheckingThread extends Thread
    {
        ChecksumCacheManager manager;

        int sleepInterval = 100; // 100 ms
        int timeSlept;
        int maxTime = 3000;      // 3 secs
        int tolerance = 1000;    // 1 sec

        private CheckingThread(ChecksumCacheManager manager)
        {
            this.manager = manager;
        }

        @Override
        public void run()
        {

            try
            {
                while (timeSlept < (maxTime + tolerance))
                {
                    if (manager.getSize() == 1)
                    {
                        break; // This was expected
                    }

                    if (timeSlept > maxTime)
                    {
                        System.out.println("The process has exceeded the defined limit of " + maxTime + " by " + (timeSlept - maxTime) + " ms...");
                    }

                    sleep(sleepInterval);
                    timeSlept += sleepInterval;
                }
            }
            catch (InterruptedException e)
            {
                // This is okay.
            }
        }

        public ChecksumCacheManager getManager()
        {
            return manager;
        }

        public int getSleepInterval()
        {
            return sleepInterval;
        }

        public int getTimeSlept()
        {
            return timeSlept;
        }

        public int getMaxTime()
        {
            return maxTime;
        }

        public int getTolerance()
        {
            return tolerance;
        }

    }

}
