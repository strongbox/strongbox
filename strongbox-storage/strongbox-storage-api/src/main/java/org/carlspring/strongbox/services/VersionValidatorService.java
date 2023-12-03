package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;

import java.util.Set;

/**
 * @author mtodorov
 */
public interface VersionValidatorService
{

    Set<ArtifactCoordinatesValidator> getVersionValidators();

}
