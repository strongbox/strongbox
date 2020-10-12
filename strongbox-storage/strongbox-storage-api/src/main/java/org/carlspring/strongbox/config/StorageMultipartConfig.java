package org.carlspring.strongbox.config;

import org.carlspring.strongbox.config.util.UpdatableMultipartConfigElement;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;
import javax.servlet.MultipartConfigElement;

@EnableConfigurationProperties(MultipartProperties.class)
public class StorageMultipartConfig
{

    @Inject
    private MultipartProperties multipartProperties;

    @Bean
    @Qualifier("multipartConfigElement")
    public MultipartConfigElement multipartConfigElement()
    {
        MultipartConfigElement multipartConfigElement = multipartProperties.createMultipartConfig();
        return new UpdatableMultipartConfigElement(multipartConfigElement.getLocation(),
                                                   multipartConfigElement.getMaxFileSize(),
                                                   multipartConfigElement.getMaxRequestSize(),
                                                   multipartConfigElement.getFileSizeThreshold());
    }

}
