package org.carlspring.strongbox.storage.validation.artifact.version;

import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

/**
 * @author carlspring
 */
public interface VersionValidator extends ArtifactCoordinatesValidator
{

    void register();

    default boolean isRelease(String version)
    {
        return !isSnapshot(version);
    }

    default boolean isSnapshot(String version)
    {
        // Note: This is a slight deviation from the SemVer spec
        return version != null && version.endsWith("-SNAPSHOT");
    }

}
