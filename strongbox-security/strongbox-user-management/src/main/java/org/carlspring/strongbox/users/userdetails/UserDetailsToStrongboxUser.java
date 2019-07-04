package org.carlspring.strongbox.users.userdetails;

import java.util.function.Function;

import org.carlspring.strongbox.users.domain.UserData;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserDetailsToStrongboxUser extends Function<UserDetails, UserData>
{

}
