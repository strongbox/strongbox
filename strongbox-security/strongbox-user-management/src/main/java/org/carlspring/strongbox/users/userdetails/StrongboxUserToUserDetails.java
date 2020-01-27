package org.carlspring.strongbox.users.userdetails;

import java.util.function.Function;

import org.carlspring.strongbox.domain.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface StrongboxUserToUserDetails extends Function<User, UserDetails>
{

}
