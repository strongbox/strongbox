package org.carlspring.strongbox.configuration;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;

import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class CorsConfiguration
{

    private final List<String> allowedOrigins;

    private final List<String> allowedMethods;

    private final List<String> allowedHeaders;

    private final List<String> exposedHeaders;

    private final Boolean allowCredentials;

    private final Long maxAge;

    public CorsConfiguration(final MutableCorsConfiguration source)
    {
        this.allowedOrigins = immuteList(source.getAllowedOrigins());
        this.allowedMethods = immuteList(source.getAllowedMethods());
        this.allowedHeaders = immuteList(source.getAllowedHeaders());
        this.exposedHeaders = immuteList(source.getExposedHeaders());
        this.allowCredentials = source.getAllowCredentials();
        this.maxAge = source.getMaxAge();
    }

    private <T> List<T> immuteList(final List<T> source)
    {
        return source != null ? ImmutableList.copyOf(source) : Collections.emptyList();
    }

    public List<String> getAllowedOrigins()
    {
        return allowedOrigins;
    }

    public List<String> getAllowedMethods()
    {
        return allowedMethods;
    }

    public List<String> getAllowedHeaders()
    {
        return allowedHeaders;
    }

    public List<String> getExposedHeaders()
    {
        return exposedHeaders;
    }

    public Boolean getAllowCredentials()
    {
        return allowCredentials;
    }

    public Long getMaxAge()
    {
        return maxAge;
    }
}
