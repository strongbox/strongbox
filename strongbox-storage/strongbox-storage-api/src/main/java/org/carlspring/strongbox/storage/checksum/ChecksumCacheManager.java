package org.carlspring.strongbox.storage.checksum;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All artifacts should pass through here.
 * Any deployed file which doesn't end in a checksum format (md5, sha1, gpg)
 * should add a cachedChecksum. When an actual checksum is deployed,
 * (which is right after the artifact or metadata file has been deployed),
 * the cache should be queried for a match.
 *
 * If:
 * - a match is found and matches, remove it from the cache. (If the checksums set
 *   is empty, remove the respective Checksum from the cachedChecksums).
 * - a match is found, but does not match, trigger an event and log this, then remove
 *   the checksum from the cache. (If the checksums set is empty, remove the respective
 *   Checksum from the cachedChecksums).
 * - a checksum is not claimed within cachedChecksumLifetime, trigger an event and log
 *   this, then remove the checksum from the cache. (If this checksums set is empty,
 *   remove the respective Checksum from the cachedChecksums).
 *
 * @author mtodorov
 */
public class ChecksumCacheManager
{

    private static Logger logger = LoggerFactory.getLogger(ChecksumCacheManager.class);

    /**
     * Key:     Artifact path
     * Value:   Artifact checksum.
     */
    private Map<String, ArtifactChecksum> cachedChecksums = new LinkedHashMap<String, ArtifactChecksum>();

    /**
     * Specifies how long to keep the cached checksums.
     *
     * The default is five minutes.
     */
    private long cachedChecksumLifetime = 5 * 60000;

    /**
     * Specifies at what interval to check if the checksums have expired.
     * The default is to check once every minute.
     */
    private long cachedChecksumExpiredCheckInterval = 60000L;


    public ChecksumCacheManager()
    {
    }

    public boolean containsArtifactPath(String artifactPath)
    {
        final boolean containsChecksum = cachedChecksums.containsKey(artifactPath);
        if (containsChecksum)
        {
            logger.debug("Cache contains artifact path '" + artifactPath + "'.");
        }

        return containsChecksum;
    }

    public String getArtifactChecksum(String artifactBasePath, String algorithm)
    {
        if (!cachedChecksums.containsKey(artifactBasePath))
        {
            return null;
        }

        final ArtifactChecksum artifactChecksum = cachedChecksums.get(artifactBasePath);
        final String checksum = artifactChecksum.getChecksum(algorithm);
        if (checksum != null)
        {
            logger.debug("Found checksum '" + checksum + "' [" + algorithm + "]"  + " for '" + artifactBasePath + "' in cache.");
        }

        return checksum;
    }

    public boolean validateChecksum(String artifactPath, String algorithm, String checksum)
    {
        return getArtifactChecksum(artifactPath, algorithm).equals(checksum);
    }

    public synchronized void addArtifactChecksum(String artifactBasePath,
                                                 String algorithm,
                                                 String checksum)
    {
        logger.debug("Adding checksum '" + checksum + "' [" + algorithm + "]"  + " for '" + artifactBasePath + "' in cache.");

        if (cachedChecksums.containsKey(artifactBasePath))
        {
            final ArtifactChecksum artifactChecksum = cachedChecksums.get(artifactBasePath);
            artifactChecksum.addChecksum(algorithm, checksum);
        }
        else
        {
            ArtifactChecksum artifactChecksum = new ArtifactChecksum();
            artifactChecksum.addChecksum(algorithm, checksum);

            cachedChecksums.put(artifactBasePath, artifactChecksum);
        }
    }

    public synchronized void removeArtifactChecksum(String artifactBasePath, String algorithm)
    {
        logger.debug("Removing " + algorithm + " checksum for artifact '" + artifactBasePath + "' from cache.");
        cachedChecksums.get(artifactBasePath).removeChecksum(algorithm);
    }

    public synchronized void removeArtifactChecksum(String artifactBasePath)
    {
        logger.debug("Removing artifact '" + artifactBasePath + "' from cache.");
        cachedChecksums.remove(artifactBasePath);
    }

    public synchronized void removeExpiredChecksums()
    {
        for (String checksum : getExpiredChecksums())
        {
            removeArtifactChecksum(checksum);
        }
    }

    private Set<String> getExpiredChecksums()
    {
        Set<String> expiredChecksums = new LinkedHashSet<String>();

        for (Map.Entry<String, ArtifactChecksum> stringArtifactChecksumEntry : cachedChecksums.entrySet())
        {
            ArtifactChecksum checksum = stringArtifactChecksumEntry.getValue();

            if (System.currentTimeMillis() - checksum.getLastAccessed() > cachedChecksumLifetime)
            {
                expiredChecksums.add(stringArtifactChecksumEntry.getKey());
            }
        }

        return expiredChecksums;
    }

    public long getCachedChecksumLifetime()
    {
        return cachedChecksumLifetime;
    }

    public void setCachedChecksumLifetime(long cachedChecksumLifetime)
    {
        this.cachedChecksumLifetime = cachedChecksumLifetime;
    }

    public long getCachedChecksumExpiredCheckInterval()
    {
        return cachedChecksumExpiredCheckInterval;
    }

    public void setCachedChecksumExpiredCheckInterval(long cachedChecksumExpiredCheckInterval)
    {
        this.cachedChecksumExpiredCheckInterval = cachedChecksumExpiredCheckInterval;
    }

    public long getSize()
    {
        return cachedChecksums.size();
    }

    public void startMonitor()
    {
        new CachedChecksumExpirer();
    }

    private class CachedChecksumExpirer
            extends Thread
    {

        private CachedChecksumExpirer()
        {
            start();
        }

        @Override
        public void run()
        {
            try
            {
                //noinspection InfiniteLoopStatement
                while (true)
                {
                    Thread.sleep(getCachedChecksumExpiredCheckInterval());
                    removeExpiredChecksums();
                }
            }
            catch (InterruptedException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
