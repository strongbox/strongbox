package org.carlspring.strongbox.authentication.api;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class Authenticators
{

    @Inject
    private List<Authenticator> authenticators;

    public List<Authenticator> getAuthenticators()
    {
        return authenticators;
    }
}
