package org.carlspring.strongbox.data.tx;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.carlspring.strongbox.data.domain.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

@Aspect
@Component
@Order(OEntityUnproxyAspect.ORDER)
public class OEntityUnproxyAspect implements Ordered
{
    public static final int ORDER = 110;

    private static final Logger logger = LoggerFactory.getLogger(OEntityUnproxyAspect.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(@org.springframework.transaction.annotation.Transactional * *(..)) "
            + "|| (execution(public * ((@org.springframework.transaction.annotation.Transactional *)+).*(..)) "
            + "&& within(@org.springframework.transaction.annotation.Transactional *))) "
            + "&& !cflowbelow(execution(* OEntityUnproxyAspect.*(..)) ")
    public Object transactional(ProceedingJoinPoint jp)
        throws Throwable
    {
        logger.debug("Transactional metod execution start.");
        Object result = jp.proceed();
        logger.debug("Transactional metod execution end.");
        return unproxy(result);
    }

    private Object unproxy(Object result)
    {
        if (result == null)
        {
            return null;
        }
        if (result instanceof GenericEntity)
        {
            result = ((OObjectDatabaseTx) entityManager.getDelegate()).detachAll(result, true);
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

    @Override
    public int getOrder()
    {
        return ORDER;
    };

}
