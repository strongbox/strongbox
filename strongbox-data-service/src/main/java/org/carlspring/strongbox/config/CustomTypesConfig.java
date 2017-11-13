package org.carlspring.strongbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@EnableAspectJAutoProxy
public class CustomTypesConfig
{

    @Bean
    public MyAspect myAspect()
    {
        return new MyAspect();
    }
}
