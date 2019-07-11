package org.carlspring.strongbox.validation.configuration.routing;

import org.carlspring.strongbox.forms.storage.routing.RoutingRuleRepositoryForm;
import org.carlspring.strongbox.services.ConfigurationManagementService;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

public class RoutingRuleRepositoryFormValidator
        implements ConstraintValidator<RoutingRuleRepositoryFormValid, RoutingRuleRepositoryForm>
{

    @Inject
    private ConfigurationManagementService configurationManagementService;

    private String message;

    @Override
    public void initialize(RoutingRuleRepositoryFormValid constraintAnnotation)
    {
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(RoutingRuleRepositoryForm form,
                           ConstraintValidatorContext context)
    {
        Boolean valid = false;

        try
        {
            String storageIdValue = StringUtils.trimToNull(form.getStorageId());
            String repositoryIdValue = StringUtils.trimToNull(form.getRepositoryId());

            if (storageIdValue == null && repositoryIdValue == null)
            {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                       .addPropertyNode("storageId")
                       .addConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                       .addPropertyNode("repositoryId")
                       .addConstraintViolation();
            }
            else
            {
                valid = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return valid;
    }
}