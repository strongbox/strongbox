package org.carlspring.strongbox.validation.configuration.routing;

import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;
import static org.carlspring.strongbox.db.schema.Properties.STORAGE_ID;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.forms.storage.routing.RoutingRuleRepositoryForm;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingRuleRepositoryFormValidator
        implements ConstraintValidator<RoutingRuleRepositoryFormValid, RoutingRuleRepositoryForm>
{

    private final Logger logger = LoggerFactory.getLogger(RoutingRuleRepositoryFormValidator.class);

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
                       .addPropertyNode(STORAGE_ID)
                       .addConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                       .addPropertyNode(REPOSITORY_ID)
                       .addConstraintViolation();
            }
            else
            {
                valid = true;
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }

        return valid;
    }
}