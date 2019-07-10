package org.carlspring.strongbox.users.userdetails;

import java.util.function.Function;

import org.carlspring.strongbox.users.domain.UserData;
import org.springframework.security.core.userdetails.UserDetails;

public interface StrongboxUserToUserDetails extends Function<UserData, UserDetails>
{

}
