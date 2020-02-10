package org.carlspring.strongbox.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.exception.RepositoryNotFoundException;
import org.carlspring.strongbox.exception.ServiceUnavailableException;
import org.carlspring.strongbox.exception.StorageNotFoundException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;

import javax.inject.Inject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;

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

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private ModelAndViewContainer modelAndViewContainer;

    @Inject
    private NativeWebRequest nativeWebRequest;

    @Inject
    private WebDataBinderFactory webDataBinderFactory;

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

    @Test
    public void testResolveArgument()
        throws MissingPathVariableException
    {

        mockStoragesAndRepositories();

        Map<String, String> uriTemplateVars = new HashMap<>();
        uriTemplateVars.put("storageId", "storage-pypi");
        uriTemplateVars.put("repositoryId", "pypi-releases");
        nativeWebRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVars,
                                      RequestAttributes.SCOPE_REQUEST);

        MethodParameter uploadArtifactWithRepositoryMappingMethod = getMethodParameter("uploadArtifactWithRepositoryMapping",
                                                                                       Repository.class);
        Repository repository = (Repository) repositoryMethodArgumentResolver.resolveArgument(uploadArtifactWithRepositoryMappingMethod,
                                                                                              modelAndViewContainer,
                                                                                              nativeWebRequest,
                                                                                              webDataBinderFactory);
        assertNotNull(repository);

        // ServiceUnavailableException Test Case
        MethodParameter uploadArtifactForRepositoryNotInServiceMethod = getMethodParameter("uploadArtifactForRepositoryNotInService",
                                                                                           Repository.class);
        ServiceUnavailableException serviceUnavailableException = Assertions.assertThrows(ServiceUnavailableException.class,
                                                                                          () -> repositoryMethodArgumentResolver.resolveArgument(uploadArtifactForRepositoryNotInServiceMethod,
                                                                                                                                                 modelAndViewContainer,
                                                                                                                                                 nativeWebRequest,
                                                                                                                                                 webDataBinderFactory));
        assertEquals(String.format(RepositoryMethodArgumentResolver.NOT_IN_SERVICE_REPOSITORY_MESSAGE,
                                   uriTemplateVars.get("storageId"), uriTemplateVars.get("repositoryId")),
                     serviceUnavailableException.getMessage());

        // StorageNotFoundException Test Case
        uriTemplateVars.put("storageId", "storage-nuget");
        uriTemplateVars.put("repositoryId", "pypi-releases");
        nativeWebRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVars,
                                      RequestAttributes.SCOPE_REQUEST);
        StorageNotFoundException storageNotFoundException = Assertions.assertThrows(StorageNotFoundException.class,
                                                                                    () -> repositoryMethodArgumentResolver.resolveArgument(uploadArtifactWithRepositoryMappingMethod,
                                                                                                                                           modelAndViewContainer,
                                                                                                                                           nativeWebRequest,
                                                                                                                                           webDataBinderFactory));
        assertEquals(String.format(RepositoryMethodArgumentResolver.NOT_FOUND_STORAGE_MESSAGE,
                                   uriTemplateVars.get("storageId")),
                     storageNotFoundException.getMessage());

        // RepositoryNotFoundException Test Case
        uriTemplateVars.put("storageId", "storage-pypi");
        uriTemplateVars.put("repositoryId", "maven-releases");
        nativeWebRequest.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVars,
                                      RequestAttributes.SCOPE_REQUEST);
        RepositoryNotFoundException repositoryNotFoundException = Assertions.assertThrows(RepositoryNotFoundException.class,
                                                                                          () -> repositoryMethodArgumentResolver.resolveArgument(uploadArtifactWithRepositoryMappingMethod,
                                                                                                                                                 modelAndViewContainer,
                                                                                                                                                 nativeWebRequest,
                                                                                                                                                 webDataBinderFactory));
        assertEquals(String.format(RepositoryMethodArgumentResolver.NOT_FOUND_REPOSITORY_MESSAGE,
                                   uriTemplateVars.get("storageId"), uriTemplateVars.get("repositoryId")),
                     repositoryNotFoundException.getMessage());

    }

    private void mockStoragesAndRepositories()
    {
        Map<String, RepositoryDto> repositories = new HashMap<>();
        Map<String, StorageDto> storages = new HashMap<>();

        RepositoryDto repositoryDto = new RepositoryDto();
        repositoryDto.setId("pypi-releases");
        repositories.put(repositoryDto.getId(), repositoryDto);

        StorageDto storageDto = new StorageDto();
        storageDto.setId("storage-pypi");
        storageDto.setRepositories(repositories);
        storages.put(storageDto.getId(), storageDto);

        MutableConfiguration mutableConfiguration = new MutableConfiguration();
        mutableConfiguration.setStorages(storages);
        org.carlspring.strongbox.configuration.Configuration configuration = new org.carlspring.strongbox.configuration.Configuration(
                mutableConfiguration);

        Mockito.when(configurationManager.getConfiguration()).thenReturn(configuration);
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

        @Bean
        public ModelAndViewContainer modelAndViewContainer()
        {
            return Mockito.mock(ModelAndViewContainer.class);
        }

        @Bean
        public NativeWebRequest nativeWebRequest()
        {
            return new ServletWebRequest(new MockHttpServletRequest());
        }

        @Bean
        public WebDataBinderFactory webDataBinderFactory()
        {
            return Mockito.mock(WebDataBinderFactory.class);
        }
    }
}
