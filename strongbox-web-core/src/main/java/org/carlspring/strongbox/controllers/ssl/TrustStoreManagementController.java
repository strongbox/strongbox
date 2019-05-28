package org.carlspring.strongbox.controllers.ssl;

import org.carlspring.strongbox.security.certificates.AbstractKeyStoreManager;
import org.carlspring.strongbox.security.certificates.TrustStoreManager;

import javax.inject.Inject;

import io.swagger.annotations.Api;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.carlspring.strongbox.controllers.ssl.TrustStoreManagementController.REQUEST_MAPPING;


/**
 * @author Przemyslaw Fusik
 */
@ConditionalOnProperty(prefix = "server.ssl", name = "trust-store")
@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequestMapping(value = REQUEST_MAPPING)
@Api(value = REQUEST_MAPPING)
class TrustStoreManagementController
        extends AbstractKeyStoreManagementController
{

    static final String REQUEST_MAPPING = "/api/ssl/trustStore";

    @Inject
    private TrustStoreManager trustStoreManager;


    @Override
    AbstractKeyStoreManager getKeyStoreManager()
    {
        return trustStoreManager;
    }
}
