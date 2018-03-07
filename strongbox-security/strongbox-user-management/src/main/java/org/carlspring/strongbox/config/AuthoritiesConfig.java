package org.carlspring.strongbox.config;

import org.carlspring.strongbox.users.security.AuthorizationConfig;
import org.carlspring.strongbox.users.security.AuthorizationConfigFileManager;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class AuthoritiesConfig
{

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    private AuthorizationConfigFileManager authorizationConfigFileManager;

    @Inject
    private AuthorizationConfigProvider authorizationConfigProvider;

    @PostConstruct
    public void init()
    {
        transactionTemplate.execute((s) ->
                                    {
                                        doInit();
                                        return null;
                                    });
    }

    private void doInit()
    {
        final AuthorizationConfig config = authorizationConfigFileManager.read();
        authorizationConfigProvider.save(config);
    }
}
