package org.carlspring.strongbox.controllers.ssl;

import org.carlspring.strongbox.config.IntegrationTest;

import org.junit.jupiter.api.parallel.Execution;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@Execution(SAME_THREAD)
class KeyStoreManagementControllerTestIT
        extends AbstractKeyStoreManagementControllerIT
{

    @Override
    String getApiUrl()
    {
        return "/api/ssl/keyStore";
    }
}
