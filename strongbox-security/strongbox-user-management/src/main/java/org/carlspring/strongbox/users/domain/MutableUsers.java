package org.carlspring.strongbox.users.domain;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Przemyslaw Fusik
 */
public class MutableUsers
{

    private Set<MutableUser> users;

    public MutableUsers(final Set<MutableUser> users)
    {
        this.users = users;
    }

    public Set<MutableUser> getUsers()
    {
        return users;
    }

    public void setUsers(final Set<MutableUser> users)
    {
        this.users = users;
    }

    public Optional<MutableUser> findByUserName(final String username)
    {
        if (CollectionUtils.isEmpty(users))
        {
            return Optional.empty();
        }
        if (username == null)
        {
            return Optional.empty();
        }
        return users.stream().filter(user -> username.equals(user.getUsername())).findFirst();
    }
}
