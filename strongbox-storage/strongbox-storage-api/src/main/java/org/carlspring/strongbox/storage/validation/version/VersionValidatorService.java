package org.carlspring.strongbox.storage.validation.version;

import java.util.LinkedHashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
public class VersionValidatorService
{

    private Set<VersionValidator> validators = new LinkedHashSet<>();


    public VersionValidatorService()
    {
        ServiceLoader<VersionValidator> versionValidators = ServiceLoader.load(VersionValidator.class);
        for (VersionValidator versionValidator : versionValidators)
        {
            validators.add(versionValidator);
        }
    }

    public Set<VersionValidator> getValidators()
    {
        return validators;
    }

    public void setValidators(Set<VersionValidator> validators)
    {
        this.validators = validators;
    }

}
