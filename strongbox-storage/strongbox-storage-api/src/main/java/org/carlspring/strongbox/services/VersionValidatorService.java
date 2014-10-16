package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import java.util.Set;

/**
 * @author mtodorov
 */
public interface VersionValidatorService
{

    Set<VersionValidator> getValidators();

}
