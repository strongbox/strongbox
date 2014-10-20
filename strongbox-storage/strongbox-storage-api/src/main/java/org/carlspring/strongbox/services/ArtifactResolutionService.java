package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.DataCenter;
import org.carlspring.strongbox.storage.resolvers.ArtifactResolutionException;
import org.carlspring.strongbox.storage.resolvers.LocationResolver;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author mtodorov
 */
public interface ArtifactResolutionService
{

    InputStream getInputStream(String repositoryName,
                               String artifactPath)
            throws ArtifactResolutionException, IOException;

    OutputStream getOutputStream(String repositoryName,
                                 String artifactPath)
                    throws ArtifactResolutionException, IOException;

    Map<String, LocationResolver> getResolvers();

    DataCenter getDataCenter();

}
