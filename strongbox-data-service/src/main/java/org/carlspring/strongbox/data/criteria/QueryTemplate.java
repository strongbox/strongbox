package org.carlspring.strongbox.data.criteria;

import org.carlspring.strongbox.data.domain.DomainObject;

/**
 * You can perform concrete queries under concrete DB implementations with
 * implementations of {@link QueryTemplate}.
 * 
 * @author sbespalov
 *
 */
public interface QueryTemplate<R, T extends DomainObject>
{

    R select(Selector<T> s);

}
