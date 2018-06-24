package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
public class UsersDto
{

    @XmlElement(name = "user")
    private Set<UserDto> users = new LinkedHashSet<>();


    public UsersDto()
    {
    }

    public UsersDto(Set<UserDto> users)
    {
        this.users = users;
    }

    public Set<UserDto> getUsers()
    {
        return users;
    }

    public void setUsers(Set<UserDto> users)
    {
        this.users = users;
    }

    public Optional<UserDto> findByUserName(final String username)
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
