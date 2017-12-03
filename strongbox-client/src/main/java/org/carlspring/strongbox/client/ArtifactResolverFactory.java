package org.carlspring.strongbox.client;

import javax.ws.rs.client.Client;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ArtifactResolverFactory
{

    public ArtifactResolver newInstance(final Client client)
    {
        return new ArtifactResolver(client);
    }

}
