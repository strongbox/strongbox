package org.carlspring.strongbox.security.vote;

import org.carlspring.strongbox.security.user.SpringSecurityUser;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * Customization of {@link AccessDecisionVoter} according to custom access strategies for users
 * like per-repository access etc.
 *
 * @author Alex Oreshkevich
 * @see https://youtrack.carlspring.org/issue/SB-603
 */
public class CustomAccessDecisionVoter
        implements AccessDecisionVoter
{

    private static final Logger logger = LoggerFactory.getLogger(CustomAccessDecisionVoter.class);

    @Override
    public boolean supports(ConfigAttribute attribute)
    {
        // don't need any specific configuration attributes
        return true;
    }

    @Override
    public int vote(Authentication authentication,
                    Object object,
                    Collection collection)
    {
        logger.debug("Principal " + authentication.getPrincipal()
                                                  .getClass()
                                                  .getName() + "\n" + authentication.getPrincipal());
        logger.debug("Authorities " + authentication.getAuthorities());
        logger.debug("Object " + object.getClass()
                                       .getName());
        logger.debug("Collection " + collection);

        Object principal = authentication.getPrincipal();
        if (principal instanceof SpringSecurityUser)
        {

            // if principal has defined any custom access management rules, use it
            SpringSecurityUser user = (SpringSecurityUser) principal;


        }

        // by default, do not participate in voting at all
        return ACCESS_ABSTAIN;
    }

    @Override
    public boolean supports(Class clazz)
    {
        // our voter is not concerned with the secured object type
        return true;
    }
}
