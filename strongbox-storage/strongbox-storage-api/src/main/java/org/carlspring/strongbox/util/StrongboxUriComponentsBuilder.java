package org.carlspring.strongbox.util;

import org.carlspring.strongbox.configuration.ConfigurationManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import static org.carlspring.strongbox.web.Constants.ARTIFACT_ROOT_PATH;
import static org.carlspring.strongbox.web.Constants.BROWSE_ROOT_PATH;

@Component
public class StrongboxUriComponentsBuilder
        extends ServletUriComponentsBuilder
{

    public static final Logger logger = LoggerFactory.getLogger(StrongboxUriComponentsBuilder.class);

    @Inject
    protected ConfigurationManager configurationManager;

    public StrongboxUriComponentsBuilder()
    {

    }

    /**
     * Get the `baseUrl` from `strongbox.yaml` as `UriComponents` or `null` if it was not set
     *
     * @return UriComponents|null
     */
    public UriComponents getConfigurationBaseUriComponents()
    {
        UriComponents uriComponents = null;

        if (!StringUtils.isBlank(configurationManager.getConfiguration().getBaseUrl()))
        {
            String baseUrl = StringUtils.removeEnd(configurationManager.getConfiguration().getBaseUrl(), "/");
            uriComponents = UriComponentsBuilder.fromUriString(baseUrl).build();
        }

        return uriComponents;
    }

    /**
     * Returns the current requestURI (i.e. /my/absolute/path/excluding/domain/without/trailing/slash)
     *
     * @return String
     */
    public String getCurrentRequestURI()
    {
        return StringUtils.removeEnd(getBuilder(false, true, null).build().getPath(), "/");
    }

    /**
     * Returns the current requestURL (i.e. http://local.dev/my/absolute/path/without/trailing/slash)
     *
     * @return String
     */
    public URL getCurrentRequestURL()
            throws MalformedURLException
    {
        return getBuilder(false, true, null)
                       .replacePath(getCurrentRequestURI())
                       .build()
                       .toUri()
                       .toURL();
    }

    /**
     * Returns {@link UriComponentsBuilder} using the current request to configure the scheme, host, port, path and query.
     * <p>
     * If {@code removeRequestURI} is true - the returned builder will not contain the request path.
     * </p>
     * <p>
     * If {@code removeQuery} is true - the returned builder will not contain the query.
     * </p>
     * <p>
     * If a `baseUrl` has been set in the `strongbox.yaml` configuration it will overwrite the populated scheme, host,
     * port and path so that they match the `baseUrl`.
     * <p>
     * When no `baseUrl` is provided - it will automatically look up the values from the incoming
     * {@link javax.servlet.http.HttpServletRequest} and will honor `X-Forwarded-*` headers for reverse-proxy support.
     * </p>
     *
     * @param removeRequestURI If we should keep the path from the current request or remove it.
     * @param removeQuery      If we should keep the query from the current request or remove it
     * @param appendPath       Optional path to append after we have configured the builder; Used as a shortcut
     *
     * @return UriComponentsBuilder
     */
    public UriComponentsBuilder getBuilder(boolean removeRequestURI,
                                           boolean removeQuery,
                                           String appendPath)
    {
        HttpServletRequest request = getCurrentRequest();
        UriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequest(request);

        // Should we remove the initially set path? (necessary when we need to construct url from scratch)
        if (removeRequestURI)
        {
            builder.replacePath(request.getContextPath());
        }

        // Remove or keep `?some=query&params=true`
        if (removeQuery)
        {
            builder.query(null);
        }

        // Force baseUrl when it has been set in `strongbox.yaml`.
        UriComponents components = getConfigurationBaseUriComponents();
        if (components != null)
        {
            builder.scheme(components.getScheme());
            builder.host(components.getHost());
            builder.port(components.getPort());

            if (removeRequestURI && components.getPath() != null)
            {
                builder.path(components.getPath());
            }
        }

        // Append an additional path (i.e. /api/some/base/path)
        if(appendPath != null)
        {
            builder.path(appendPath);
        }

        return builder;
    }

    /**
     * Get a builder without preserving the request path. (i.e. http://localhost:48080)
     *
     * @return UriComponentsBuilder
     */
    public UriComponentsBuilder getBuilder()
    {
        return getBuilder(true, true, null);
    }

    /**
     * Shortcut to avoid doing `getBuilder().path("/api/browse|logging|etc")`
     *
     * @param appendPath
     *
     * @return
     */
    public UriComponentsBuilder getBuilder(String appendPath)
    {
        return getBuilder(true, true, appendPath);
    }


    public UriComponentsBuilder browseUriBuilder()
    {
        return getBuilder(BROWSE_ROOT_PATH);
    }

    public UriComponentsBuilder browseUriBuilder(@NonNull String storageId)
    {
        Assert.notNull(storageId, "storageId is required");

        return genericArtifactPathBuilder(browseUriBuilder(), storageId, null, null);
    }

    public UriComponentsBuilder browseUriBuilder(@NonNull String storageId,
                                                 @NonNull String repositoryId)
    {
        Assert.notNull(storageId, "storageId is required");
        Assert.notNull(repositoryId, "repositoryId is required");

        return genericArtifactPathBuilder(browseUriBuilder(), storageId, repositoryId, null);
    }

    public UriComponentsBuilder browseUriBuilder(@NonNull String storageId,
                                                 @NonNull String repositoryId,
                                                 @Nullable String path)
    {
        Assert.notNull(storageId, "storageId is required");
        Assert.notNull(repositoryId, "repositoryId is required");

        return genericArtifactPathBuilder(browseUriBuilder(), storageId, repositoryId, path);
    }


    public UriComponentsBuilder browseUriBuilder(@NonNull String storageId,
                                                 @NonNull String repositoryId,
                                                 URI pathResource)
    {
        String path = pathResource != null ? pathResource.getPath() : null;
        return browseUriBuilder(storageId, repositoryId, path);
    }

    /**
     * `http://localhost:48080/storages`
     *
     * @return UriComponentsBuilder
     */
    public UriComponentsBuilder storageUriBuilder()
    {
        return getBuilder(ARTIFACT_ROOT_PATH);
    }

    /**
     * `http://localhost:48080/storages/{storageId}`
     *
     * @return UriComponentsBuilder
     * @throws IllegalArgumentException
     */
    public UriComponentsBuilder storageUriBuilder(@NonNull String storageId)
    {
        Assert.notNull(storageId, "storageId is required");
        return genericArtifactPathBuilder(storageUriBuilder(), storageId, null, null);
    }

    /**
     * `http://localhost:48080/storages/{storageId}/{repositoryId}`
     *
     * @return UriComponentsBuilder
     * @throws IllegalArgumentException
     */
    public UriComponentsBuilder storageUriBuilder(@NonNull String storageId,
                                                  @NonNull String repositoryId)
    {
        Assert.notNull(storageId, "storageId is required");
        Assert.notNull(repositoryId, "repositoryId is required");

        return genericArtifactPathBuilder(storageUriBuilder(), storageId, null, null);
    }

    /**
     * `http://localhost:48080/storages/{storageId}/{repositoryId}` or
     * `http://localhost:48080/storages/{storageId}/{repositoryId}/{artifactPath}`
     *
     * @param storageId
     * @param repositoryId
     * @param artifactPath
     *
     * @return UriComponentsBuilder
     * @throws IllegalArgumentException
     */
    public UriComponentsBuilder storageUriBuilder(@NonNull String storageId,
                                                  @NonNull String repositoryId,
                                                  String artifactPath)
    {
        Assert.notNull(storageId, "storageId is required");
        Assert.notNull(repositoryId, "repositoryId is required");

        return genericArtifactPathBuilder(storageUriBuilder(), storageId, repositoryId, artifactPath);
    }

    /**
     * @param storageId
     * @param repositoryId
     * @param artifactResource
     * @return
     * @throws IllegalArgumentException
     */
    public UriComponentsBuilder storageUriBuilder(@NonNull String storageId,
                                                  @NonNull String repositoryId,
                                                  URI artifactResource)
    {
        String artifactPath = artifactResource != null ? artifactResource.getPath() : null;
        return storageUriBuilder(storageId, repositoryId, artifactPath);
    }

    /**
     * Generic artifact path builder (used mainly for `browse` and `storage` related urls)
     *
     * @param baseBuilder  The base builder to which we will be appending additional paths.
     * @param storageId    The storageId (or null)
     * @param repositoryId The repositoryId (or null; will be ignored if storageId is null)
     * @param artifactPath The artifact path (or null; will be ignored if repoId is null)
     *
     * @return UriComponentsBuilder
     */
    protected UriComponentsBuilder genericArtifactPathBuilder(UriComponentsBuilder baseBuilder,
                                                              @Nullable String storageId,
                                                              @Nullable String repositoryId,
                                                              @Nullable String artifactPath)
    {
        if (StringUtils.isNotBlank(storageId))
        {
            HashMap<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("storageId", storageId);
            baseBuilder.pathSegment("{storageId}");

            if (StringUtils.isNotBlank(repositoryId))
            {
                uriVariables.put("repositoryId", repositoryId);
                baseBuilder.pathSegment("{repositoryId}");

                // Note - it is not possible to use a `pathSegment` here since the URI variable will escape `/` and
                // lead to escaped paths (i.e. some%2Fartifact%2Fpath%2Fto%2Fpom.xml). If you need that - pass `null`
                // artifactPath and manually add `.path("my/path/")`
                if (artifactPath != null)
                {
                    baseBuilder.path(StringUtils.removeStart(artifactPath, "/"));
                }
            }

            baseBuilder.uriVariables(uriVariables);
        }

        return baseBuilder;
    }

}
