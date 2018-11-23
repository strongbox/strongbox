package org.carlspring.strongbox.users.domain;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;
import org.carlspring.strongbox.users.dto.UserPathPrivelegiesReadContract;
import org.carlspring.strongbox.users.dto.UserPathPrivilegesDto;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import static java.util.stream.Collectors.toSet;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class AccessModelPathPrivileges
        implements Serializable, UserPathPrivelegiesReadContract
{

    private final String path;

    /**
     *  If true, allows to set privileges within path and all subdirectories
     */
    private final boolean wildcard;

    private final Set<AccessModelPrivelege> privileges;

    public AccessModelPathPrivileges(final UserPathPrivilegesDto delegate)
    {
        this.path = delegate.getPath();
        this.wildcard = delegate.isWildcard();
        this.privileges = immutePrivileges(delegate.getPrivileges());
    }


    private Set<AccessModelPrivelege> immutePrivileges(final Set<PrivilegeDto> source)
    {
        return source != null ?
               ImmutableSet.copyOf(source.stream().map(p -> new AccessModelPrivelege(p)).collect(toSet())) :
               Collections.emptySet();
    }

    public String getPath()
    {
        return path;
    }

    public boolean isWildcard()
    {
        return wildcard;
    }

    public Set<AccessModelPrivelege> getPrivileges()
    {
        return privileges;
    }
}
