package org.carlspring.strongbox.validation.users;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

/**
 * @author Pablo Tirado
 */
public class UniqueUserNameValidator
        implements ConstraintValidator<UniqueUserName, String>
{

    private UserService userService;

    public UniqueUserNameValidator(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void initialize(UniqueUserName constraint)
    {
        // Empty method, not used.
    }

    @Override
    public boolean isValid(String userName,
                           ConstraintValidatorContext context)
    {
        User user = userService.findByUserName(userName);
        return StringUtils.isEmpty(userName) || user == null;
    }

}
