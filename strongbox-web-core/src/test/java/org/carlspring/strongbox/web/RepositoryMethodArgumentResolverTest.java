package org.carlspring.strongbox.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;

import javax.inject.Inject;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ankit.tomar
 */
@SpringBootTest
@ContextConfiguration(classes = RepositoryMethodArgumentResolverTest.RepositoryMethodArgumentResolverConfiguration.class)
@ActiveProfiles({ "test", "RepositoryMethodArgumentResolverTest" })
public class RepositoryMethodArgumentResolverTest
{

    @Inject
    private RepositoryMethodArgumentResolver repositoryMethodArgumentResolver;

    @Test
    public void testSupportsParameter()
    {

        boolean supportsParameterTrueCase1 = repositoryMethodArgumentResolver.supportsParameter(getMethodParameter("uploadArtifactWithRepositoryMapping",
                                                                                                                   Repository.class));
        assertTrue(supportsParameterTrueCase1);
        boolean supportsParameterTrueCase2 = repositoryMethodArgumentResolver.supportsParameter(getMethodParameter("uploadArtifactForRepositoryNotInService",
                                                                                                                   Repository.class));
        assertTrue(supportsParameterTrueCase2);
        boolean supportsParameterFalseCase1 = repositoryMethodArgumentResolver.supportsParameter(getMethodParameter("uploadArtifactWithoutRepositoryMapping",
                                                                                                                    Repository.class));
        assertFalse(supportsParameterFalseCase1);
        boolean supportsParameterFalseCase2 = repositoryMethodArgumentResolver.supportsParameter(getMethodParameter("uploadArtifactWithRepositoryMappingAndInvalidRepository",
                                                                                                                    RepositoryData.class));
        assertFalse(supportsParameterFalseCase2);
    }

    private MethodParameter getMethodParameter(String methodName,
                                               Class<?>... paramTypes)
    {
        Method method = ReflectionUtils.findMethod(TestRepositoryMappingController.class, methodName,
                                                   paramTypes);
        return new MethodParameter(method, 0);
    }

    @RestController
    private class TestRepositoryMappingController
    {

        @PutMapping(path = "/{storageId}/{repositoryId}/uploadArtifactWithoutRepositoryMapping")
        void uploadArtifactWithoutRepositoryMapping(Repository repository)
        {
        }

        @PutMapping(path = "/{storageId}/{repositoryId}/uploadArtifactWithRepositoryMapping")
        void uploadArtifactWithRepositoryMapping(@RepositoryMapping Repository repository)
        {
        }

        @PutMapping(path = "/{storageId}/{repositoryId}/uploadArtifactWithRepositoryMappingAndInvalidRepository")
        void uploadArtifactWithRepositoryMappingAndInvalidRepository(@RepositoryMapping RepositoryData repository)
        {
        }

        @PutMapping(path = "/{storageId}/{repositoryId}/uploadArtifactForRepositoryNotInService")
        void uploadArtifactForRepositoryNotInService(@RepositoryMapping(inServiceRepository = false) Repository repository)
        {
        }

    }

    @Configuration
    @Profile("RepositoryMethodArgumentResolverTest")
    public static class RepositoryMethodArgumentResolverConfiguration
    {

        @Bean
        public RepositoryMethodArgumentResolver repositoryMethodArgumentResolver()
        {
            return new RepositoryMethodArgumentResolver();
        }

        @Bean
        public ConfigurationManager configurationManager()
        {
            return Mockito.mock(ConfigurationManager.class);
        }

        @Bean
        public ConfigurationManagementService configurationManagementService()
        {
            return Mockito.mock(ConfigurationManagementService.class);
        }
    }
}
