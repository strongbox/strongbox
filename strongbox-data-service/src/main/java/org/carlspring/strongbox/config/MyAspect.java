package org.carlspring.strongbox.config;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * @author Przemyslaw Fusik
 */
@Aspect
public class MyAspect
{

    @Pointcut("execution(* com.orientechnologies.orient.object.db.ODatabasePojoAbstract.convertParameter(..)) && args(localDateTime,..)")
    public void thePointcut(LocalDateTime localDateTime)
    {
    }


    @Around("thePointcut(localDateTime)")
    public Object theAdvice(ProceedingJoinPoint joinPoint,
                            LocalDateTime localDateTime)
    {
        return null;
    }

}
