package org.carlspring.strongbox.storage.validation.version;

import org.carlspring.strongbox.storage.validation.PypiArtifactCoordinatesValidator;

/**
 * @author sainalshah
 */
public interface PypiVersionValidator
        extends PypiArtifactCoordinatesValidator
{


    boolean isPreRelease(String version);

    boolean isPostRelease(String version);

    boolean isDevelopmentalRelease(String version);

    boolean isLocalVersionIdentifierRelease(String version);

    boolean isFinalRelease(String version);
}
