package org.carlspring.strongbox.config;

import org.carlspring.strongbox.config.util.UpdatableMultipartConfigElement;
import org.carlspring.strongbox.filter.ArtifactSizeFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import javax.servlet.MultipartConfigElement;

@Configuration
@EnableConfigurationProperties(MultipartProperties.class)
public class StorageMultipartConfig
{

    @Inject
    private MultipartProperties multipartProperties;

    @Inject

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

    @Bean
    public ArtifactSizeFilter artifactSizeFilter()
    {
        return new ArtifactSizeFilter();
    }

    @Bean
    public FilterRegistrationBean storageFilterRegistration()
    {
        FilterRegistrationBean registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(this.artifactSizeFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
