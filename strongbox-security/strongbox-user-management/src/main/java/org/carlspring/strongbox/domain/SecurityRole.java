package org.carlspring.strongbox.domain;

import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * @author ankit.tomar
 */
public interface SecurityRole extends DomainObject
{

    default String getRoleName()
    {
        return getUuid();
    }
}
