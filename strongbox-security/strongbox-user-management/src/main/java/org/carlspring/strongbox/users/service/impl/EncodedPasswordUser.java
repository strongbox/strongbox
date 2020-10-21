package org.carlspring.strongbox.users.service.impl;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.users.dto.User;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author sbespalov
 *
 */
public class EncodedPasswordUser implements User
{

    private final User user;
    private final PasswordEncoder passwordEncoder;

    public EncodedPasswordUser(User user,
                               PasswordEncoder passwordEncoder)
    {
        this.user = user;
        this.passwordEncoder = passwordEncoder;
    }

    public String getUsername()
    {
        return user.getUsername();
    }

    public String getPassword()
    {
        String password = user.getPassword();

        return Optional.ofNullable(password)
                       .filter(p -> StringUtils.isNoneBlank(p))
                       .map(p -> passwordEncoder.encode(p))
                       .orElse(password);
    }

    public Set<String> getRoles()
    {
        return user.getRoles();
    }

    public String getSecurityTokenKey()
    {
        return user.getSecurityTokenKey();
    }

    public boolean isEnabled()
    {
        return user.isEnabled();
    }

    public Date getLastUpdate()
    {
        return user.getLastUpdate();
    }

    public String getSourceId()
    {
        return user.getSourceId();
    }
    
}
