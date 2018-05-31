package org.carlspring.strongbox.data.criteria;

import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * You can perform concrete queries under concrete DB implementations with
 * implementations of {@link QueryTemplate}.
 * 
 * @author sbespalov
 *
 */
public interface QueryTemplate<R, T extends GenericEntity>
{

    R select(Selector<T> s);

}
