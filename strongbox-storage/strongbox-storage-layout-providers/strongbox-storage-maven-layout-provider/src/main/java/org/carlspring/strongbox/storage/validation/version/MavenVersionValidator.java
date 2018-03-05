package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.validation.MavenArtifactCoordinatesValidator;

/**
 * @author Przemyslaw Fusik
 * @author carlspring
 */
interface MavenVersionValidator
        extends MavenArtifactCoordinatesValidator
{

    default boolean isRelease(String version)
    {
        return version != null && ArtifactUtils.isReleaseVersion(version);
    }

    default boolean isSnapshot(String version)
    {
        return version != null && ArtifactUtils.isSnapshot(version);
    }

}
