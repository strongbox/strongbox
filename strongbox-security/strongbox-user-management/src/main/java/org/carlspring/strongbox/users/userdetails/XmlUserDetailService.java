package org.carlspring.strongbox.users.userdetails;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.XmlUserService.XmlUserServiceQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@XmlUserDetailService.XmlUserDetailServiceQualifier
public class XmlUserDetailService
        implements UserDetailsService
{

    private static final Logger logger = LoggerFactory.getLogger(XmlUserDetailService.class);

    @Inject
    @XmlUserServiceQualifier
    UserService userService;

    @Inject
    UserDetailsMapper springSecurityUserMapper;

    @Override
    public UserDetails loadUserByUsername(String name)
            throws UsernameNotFoundException
    {
        logger.debug("Loading user details for " + name + " ...");

        if (name == null)
        {
            throw new IllegalArgumentException("Username cannot be null.");
        }

        User user = userService.findByUserName(name);
        if (user == null)
        {
            logger.error("[authenticate] ERROR Cannot find user with that name " + name);
            throw new UsernameNotFoundException("Cannot find user with that name");
        }

        UserDetails springUser = new StrongboxUserDetails(user);
        logger.info("Authorise under " + springUser);

        return springUser;
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface XmlUserDetailServiceQualifier
    {

    }


}
