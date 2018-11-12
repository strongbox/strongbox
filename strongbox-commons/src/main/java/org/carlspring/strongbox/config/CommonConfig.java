package org.carlspring.strongbox.config;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.io",
        "org.carlspring.strongbox.net",
        "org.carlspring.strongbox.db",
        "org.carlspring.strongbox.resource",
        "org.carlspring.strongbox.rest",
        "org.carlspring.strongbox.storage.repository",
        "org.carlspring.strongbox.url",
        "org.carlspring.strongbox.util",
        "org.carlspring.strongbox.xml"
})
public class CommonConfig
{

    @Bean
    @CommonExecutorService
    ExecutorService executorService()
    {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int nThreads = (availableProcessors == 1) ? availableProcessors : (availableProcessors - 1);
        return Executors.newFixedThreadPool(nThreads);
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface CommonExecutorService
    {

    }
}
