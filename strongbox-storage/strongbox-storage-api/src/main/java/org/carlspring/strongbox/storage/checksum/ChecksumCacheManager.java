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
                                                    .anyMatch(value -> value.equals(algorithm)))
                              .findFirst()
                              .map(Map.Entry::getKey)
                              .orElse(algorithm);
    }

    public String get(final RepositoryPath artifactPath,
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
            logger.debug("Found checksum '{}' [{}] for '{}' in cache.", checksum, escapedAlgorithm, artifactPath);
        }

        return checksum;
    }

    public void put(final RepositoryPath artifactPath,
                    final String algorithm,
                    final String checksum)
    {
        final String escapedAlgorithm = getAlgorithm(algorithm);
        logger.debug("Adding checksum '{}' [{}] for '{}' in cache.", checksum, escapedAlgorithm, artifactPath);

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
