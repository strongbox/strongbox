package org.carlspring.strongbox.storage.checksum;

import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.providers.io.RepositoryPath;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * All artifacts should pass through here.
 * Any deployed file which doesn't end in a checksum format (md5, sha1, gpg)
 * should add a cachedChecksum. When an actual checksum is deployed,
 * (which is right after the artifact or metadata file has been deployed),
 * the cache should be queried for a match.
 * <p>
 * If:
 * - a match is found and matches, remove it from the cache. (If the checksums set
 * is empty, remove the respective Checksum from the cachedChecksums).
 * - a match is found, but does not match, trigger an event and log this, then remove
 * the checksum from the cache. (If the checksums set is empty, remove the respective
 * Checksum from the cachedChecksums).
 * - a checksum is not claimed within cachedChecksumLifetime, trigger an event and log
 * this, then remove the checksum from the cache. (If this checksums set is empty,
 * remove the respective Checksum from the cachedChecksums).
 *
 * @author mtodorov
 * @author Przemysław Fusik
 */
@Component
public class ChecksumCacheManager
        implements DisposableBean
{

    private static final String SHA1_KEY = "sha1";

    private static final String MD5_KEY = "md5";

    private static final Map<String, List<String>> knownAlgorithms;

    private static Logger logger = LoggerFactory.getLogger(ChecksumCacheManager.class);

    private final Cache cache;

    @Inject
    ChecksumCacheManager(final CacheManager cacheManager)
    {
        cache = cacheManager.getCache(CacheName.Artifact.CHECKSUM);
        Objects.requireNonNull(cache, "checksum cache configuration was not provided");
    }

    private static String getAlgorithm(final String algorithm)
    {
        return knownAlgorithms.entrySet()
                              .stream()
                              .filter(entry -> entry.getValue()
                                                    .stream()
                                                    .filter(value -> value.equals(algorithm))
                                                    .findFirst()
                                                    .isPresent())
                              .findFirst()
                              .map(entry -> entry.getKey())
                              .orElse(algorithm);
    }

    public String getArtifactChecksum(final RepositoryPath artifactPath,
                                      final String algorithm)
    {
        ArtifactChecksum artifactChecksum = cache.get(artifactPath.toUri(), ArtifactChecksum.class);
        if (artifactChecksum == null)
        {
            return null;
        }

        final String escapedAlgorithm = getAlgorithm(algorithm);
        final String checksum = artifactChecksum.getChecksum(escapedAlgorithm);
        if (checksum != null)
        {
            logger.debug(
                    "Found checksum '" + checksum + "' [" + escapedAlgorithm + "]" + " for '" + artifactPath +
                    "' in cache.");
        }

        return checksum;
    }

    public void addArtifactChecksum(final RepositoryPath artifactPath,
                                    final String algorithm,
                                    final String checksum)
    {
        final String escapedAlgorithm = getAlgorithm(algorithm);
        logger.debug(
                "Adding checksum '" + checksum + "' [" + escapedAlgorithm + "]" + " for '" + artifactPath +
                "' in cache.");

        ArtifactChecksum artifactChecksum = cache.get(artifactPath.toUri(), ArtifactChecksum.class);
        if (artifactChecksum == null)
        {
            artifactChecksum = new ArtifactChecksum();
        }
        artifactChecksum.addChecksum(escapedAlgorithm, checksum);

        cache.put(artifactPath.toUri(), artifactChecksum);
    }

    @Override
    public void destroy()
            throws Exception
    {
        cache.clear();
    }

    static
    {
        ImmutableMap.Builder builder = ImmutableMap.builder();

        builder.put(SHA1_KEY, Arrays.asList(SHA1_KEY,
                                            EncryptionAlgorithmsEnum.SHA1.getAlgorithm(),
                                            EncryptionAlgorithmsEnum.SHA1.getExtension()));
        builder.put(MD5_KEY, Arrays.asList(MD5_KEY,
                                           EncryptionAlgorithmsEnum.MD5.getAlgorithm(),
                                           EncryptionAlgorithmsEnum.MD5.getExtension()));
        knownAlgorithms = builder.build();
    }


}
