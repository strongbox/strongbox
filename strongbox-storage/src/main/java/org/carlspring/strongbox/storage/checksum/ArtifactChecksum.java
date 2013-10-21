package org.carlspring.strongbox.storage.checksum;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author mtodorov
 */
public class ArtifactChecksum
{

    /**
     * Key:   Algorithm
     * Value: Checksum
     */
    private Map<String, String> checksums = new LinkedHashMap<String, String>();

    /**
     * The last time this checksum object was accessed in any way.
     * Used to determine when to remove entries from the cache manager.
     */
    private long lastAccessed;

    private AtomicInteger numberOfChecksums = new AtomicInteger(0);

    private AtomicInteger numberOfValidatedChecksums = new AtomicInteger(0);


    public ArtifactChecksum()
    {
        updateLastAccessedTime();
    }

    public synchronized void addChecksum(String algorithm, String checksum)
    {
        checksums.put(algorithm, checksum);
        incrementNumberOfChecksums();
        updateLastAccessedTime();
    }

    public void updateLastAccessedTime()
    {
        lastAccessed = System.currentTimeMillis();
    }

    public synchronized void removeChecksum(String algorithm)
    {
        checksums.remove(algorithm);
        updateLastAccessedTime();
    }

    public String getChecksum(String algorithm)
    {
        updateLastAccessedTime();
        return checksums.get(algorithm);
    }

    public synchronized void incrementNumberOfChecksums()
    {
        numberOfChecksums.incrementAndGet();
    }

    public synchronized void incrementNumberOfValidatedChecksums()
    {
        numberOfValidatedChecksums.incrementAndGet();
    }

    public Map<String, String> getChecksums()
    {
        return checksums;
    }

    public void setChecksums(Map<String, String> checksums)
    {
        this.checksums = checksums;
    }

    public long getLastAccessed()
    {
        return lastAccessed;
    }

    public void setLastAccessed(long lastAccessed)
    {
        this.lastAccessed = lastAccessed;
    }

}
