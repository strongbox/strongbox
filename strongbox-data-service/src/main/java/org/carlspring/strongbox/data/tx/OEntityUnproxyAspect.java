package org.carlspring.strongbox.data.tx;

import org.carlspring.strongbox.data.criteria.DetachQueryTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Aspect
@Component
@Order(OEntityUnproxyAspect.ORDER)
public class OEntityUnproxyAspect
{

    public static final int ORDER = 110;

    private static final Logger logger = LoggerFactory.getLogger(OEntityUnproxyAspect.class);

    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * This method will wrap "Top level" transactional invocations according to rules defined with pointcut expressions.
     * "Top level" means the methods, declared with {@link Transactional}, where the new Transaction has started and
     * will be ended after method invocation complete.
     * The goal is to get unproxied value, which has been returned with intercepted (target) method.
     * The order of calling the interceptor is important, for this reason the class is declared with {@link Order} annotation.
     * The order must be before the {@link TransactionInterceptor}.
     * 
     * @param jp
     * @return
     * @throws Throwable
     */
    @Around("execution(@org.springframework.transaction.annotation.Transactional * *(..)) "
            + "|| (execution(public * ((@org.springframework.transaction.annotation.Transactional *)+).*(..)) "
            + "&& within(@org.springframework.transaction.annotation.Transactional *))) "
            + "&& !cflowbelow(execution(* OEntityUnproxyAspect.*(..)) ")
    public Object transactional(ProceedingJoinPoint jp)
        throws Throwable
    {
        logger.debug("Transactional method execution start.");

        Object result = jp.proceed();

        logger.debug("Transactional method execution end.");

        return new DetachQueryTemplate(entityManager).unproxy(result);
    }

}
