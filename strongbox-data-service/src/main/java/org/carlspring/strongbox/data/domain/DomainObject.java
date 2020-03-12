package org.carlspring.strongbox.data.domain;

import java.io.Serializable;

/**
 * @author sbespalov
 *
 */
public interface DomainObject extends Serializable
{

    String getUuid();

}