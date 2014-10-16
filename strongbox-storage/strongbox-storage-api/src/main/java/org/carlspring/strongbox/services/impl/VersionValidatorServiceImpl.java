package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.services.VersionValidatorService;
import org.carlspring.strongbox.storage.validation.version.VersionValidator;

import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class VersionValidatorServiceImpl
        implements VersionValidatorService
{

    private Set<VersionValidator> validators = new LinkedHashSet<>();


    public VersionValidatorServiceImpl()
    {
        ServiceLoader<VersionValidator> versionValidators = ServiceLoader.load(VersionValidator.class);
        for (VersionValidator versionValidator : versionValidators)
        {
            validators.add(versionValidator);
        }
    }

    @Override
    public Set<VersionValidator> getValidators()
    {
        return validators;
    }

    public void setValidators(Set<VersionValidator> validators)
    {
        this.validators = validators;
    }

}
