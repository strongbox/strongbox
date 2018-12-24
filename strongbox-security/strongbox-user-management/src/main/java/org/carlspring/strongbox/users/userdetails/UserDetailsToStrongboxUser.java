package org.carlspring.strongbox.users.userdetails;

import java.util.function.Function;

import org.carlspring.strongbox.users.domain.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsToStrongboxUser extends Function<UserDetails, User>
{

}
