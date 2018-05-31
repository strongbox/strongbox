package org.carlspring.strongbox.data.criteria;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * This class decorates underlaing {@link QueryTemplate} to detach it's result
 * from OrientDB session.
 * 
 * @author sbespalov
 *
 */
public class DetachQueryTemplate<R, T extends GenericEntity> implements QueryTemplate<R, T>
{

    private OQueryTemplate<R, T> target;

    public DetachQueryTemplate(EntityManager em)
    {
        this(new OQueryTemplate<R, T>(em));
    }

    public DetachQueryTemplate(OQueryTemplate<R, T> target)
    {
        this.target = target;
    }

    @Override
    public R select(Selector<T> s)
    {
        return (R) unproxy(target.select(s));
    }

    public Object unproxy(Object result)
    {
        if (result == null)
        {
            return null;
        }
        if (result instanceof GenericEntity)
        {
            result = target.getEmDelegate().detachAll(result, true);
        }
        else if (result instanceof Collection)
        {
            result = ((Collection) result).stream()
                                          .map(e -> unproxy(e))
                                          .collect(result instanceof Set ? Collectors.toSet() : Collectors.toList());
        }
        else if (result instanceof Optional)
        {
            result = Optional.ofNullable(unproxy(((Optional) result).orElse(null)));
        }
        return result;
    }

}
