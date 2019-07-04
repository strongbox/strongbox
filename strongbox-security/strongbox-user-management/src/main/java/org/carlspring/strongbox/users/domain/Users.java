package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.dto.UsersDto;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class Users
{

    private final Set<UserData> users;

    public Users(final UsersDto source)
    {
        this.users = immuteUsers(source.getUsers());
    }

    private Set<UserData> immuteUsers(final Set<UserDto> source)
    {
        return source != null ? ImmutableSet.copyOf(source.stream().map(UserData::new).collect(
                Collectors.toSet())) : Collections.emptySet();
    }

    public Set<UserData> getUsers()
    {
        return users;
    }
}
