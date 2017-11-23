package org.carlspring.strongbox.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientechnologies.orient.core.entity.OEntityManager;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.event",
                 "org.carlspring.strongbox.repository",
                 "org.carlspring.strongbox.providers",
                 "org.carlspring.strongbox.services",
                 "org.carlspring.strongbox.storage",
})
@Import({ EventsConfig.class })
public class NpmLayoutProviderConfig
{
    
    @Inject
    private OEntityManager oEntityManager;

    @Inject
    private TransactionTemplate transactionTemplate;

    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) -> {
            oEntityManager.registerEntityClass(NpmArtifactCoordinates.class);
            return null;
        });
    }
    
    @Bean
    public ObjectMapper npmJackasonMapper()
    {
        return new ObjectMapper();
    }
    
}
