package org.carlspring.strongbox.artifact.generator;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

/**
 * @author sbespalov
 *
 */
public interface ArtifactGenerator
{

    int DEFAULT_BYTES_SIZE = 1000000;

    /**
     * Generate artifact with POM and checksums. Adds additional file with random content with given size inside JAR.
     * Whole size of artifact will be larger than given size.
     * @param id {@link String} id of artifact - should contains minimum one ':'
     * @param version {@link String} version of artifact, could contains 'SNAPSHOT` phrase
     * @param size additional size of file inside artifact
     * @return {@link Path} path to the artifact
     * @throws IOException in case of failure
     */
    Path generateArtifact(String id,
                          String version,
                          long size)
        throws IOException;

    /**
     * Generate artifact with POM and checksums. Adds additional file with random content with given size inside JAR.
     * Whole size of artifact will be larger than given size.
     * @param uri {@link URI} URI of artifact
     * @param size additional size of file inside artifact
     * @return {@link Path} path to the artifact
     * @throws IOException in case of failure
     */
    Path generateArtifact(URI uri,
                          long size)
        throws IOException;

}
