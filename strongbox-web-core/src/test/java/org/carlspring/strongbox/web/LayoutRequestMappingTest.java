package org.carlspring.strongbox.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;
import java.io.IOException;

import org.carlspring.strongbox.configuration.StoragesConfigurationManager;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Pablo Tirado
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { LayoutRequestMappingTest.TestLayoutController.class,
                            LayoutRequestMappingTest.AnotherLayoutController.class })
@ActiveProfiles({ "test",
                  "LayoutRequestMappingTestConfig" })
public class LayoutRequestMappingTest
{

    private static final String STORAGE0 = "storage0";

    private static final String REPOSITORY_RELEASES = "releases";

    private static final String REPOSITORY_RELEASES_2 = "another-releases";

    private static final String TEST_LAYOUT_ALIAS = "test-layout-alias";

    private static final String ANOTHER_LAYOUT_ALIAS = "another-layout-alias";

    @Inject
    private MockMvc mockMvc;

    @Inject
    private StoragesConfigurationManager storagesConfigurationManager;

    @Inject
    private TestLayoutController testLayoutController;

    @Inject
    private AnotherLayoutController anotherLayoutController;

    @BeforeEach
    public void setup()
    {
        StorageDto storage = new StorageDto(STORAGE0);
        when(storagesConfigurationManager.getStorage(STORAGE0)).thenReturn(storage);

        RepositoryDto testLayoutRepository = new RepositoryDto(REPOSITORY_RELEASES);
        testLayoutRepository.setStorage(storage);
        testLayoutRepository.setLayout(TEST_LAYOUT_ALIAS);
        when(storagesConfigurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES))
                .thenReturn(testLayoutRepository);

        RepositoryDto anotherRepository = new RepositoryDto(REPOSITORY_RELEASES_2);
        anotherRepository.setStorage(storage);
        anotherRepository.setLayout(ANOTHER_LAYOUT_ALIAS);
        when(storagesConfigurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES_2))
                .thenReturn(anotherRepository);
    }

    @WithMockUser(authorities = "CONFIGURATION_VIEW_STORAGE_CONFIGURATION")
    @Test
    public void testLayoutRequestMapping()
            throws Exception
    {
        final String url = "/storages/{storageId}/{repositoryId}/{artifactPath}";
        final String artifactPath = "path/to/artifact";

        //Verify `test-layout-alias` mapping
        mockMvc.perform(get(url, STORAGE0, REPOSITORY_RELEASES, artifactPath))
               .andDo(print())
               .andExpect(status().isOk());

        verify(testLayoutController).download(STORAGE0, REPOSITORY_RELEASES);

        //Verify `another-layout-alias` mapping
        mockMvc.perform(get(url, STORAGE0, REPOSITORY_RELEASES_2, artifactPath))
               .andDo(print())
               .andExpect(status().isOk());

        verify(anotherLayoutController).download(STORAGE0, REPOSITORY_RELEASES_2);

        //Verify 404
        mockMvc.perform(get(url, STORAGE0, "no-such-repository", artifactPath))
               .andDo(print())
               .andExpect(status().isNotFound());

        verifyZeroInteractions(testLayoutController);
        verifyZeroInteractions(anotherLayoutController);
    }

    @Controller
    @LayoutRequestMapping(TEST_LAYOUT_ALIAS)
    interface TestLayoutController
    {

        @GetMapping(path = "/{storageId}/{repositoryId}/**")
        void download(@PathVariable(name = "storageId") String storageId,
                      @PathVariable(name = "repositoryId") String repositoryId)
                throws IOException;

    }

    @Controller
    @LayoutRequestMapping(ANOTHER_LAYOUT_ALIAS)
    interface AnotherLayoutController
    {

        @GetMapping(path = "/{storageId}/{repositoryId}/**")
        void download(@PathVariable(name = "storageId") String storageId,
                      @PathVariable(name = "repositoryId") String repositoryId)
                throws IOException;

    }

    @Configuration
    @Profile("LayoutRequestMappingTestConfig")
    static class LayoutRequestMappingConfiguration
    {

        @Bean
        TestLayoutController testLayoutController()
        {
            return Mockito.mock(TestLayoutController.class);
        }

        @Bean
        AnotherLayoutController anotherLayoutController()
        {
            return Mockito.mock(AnotherLayoutController.class);
        }

        @Bean
        StoragesConfigurationManager configurationManager()
        {
            return Mockito.mock(StoragesConfigurationManager.class);
        }

        @Bean
        WebMvcRegistrations webMvcRegistrations()
        {
            return new WebMvcRegistrations()
            {

                @Override
                public RequestMappingHandlerMapping getRequestMappingHandlerMapping()
                {
                    return new CustomRequestMappingHandlerMapping();
                }

            };
        }
    }
}
