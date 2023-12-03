package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.certificates.KeyStoreManager;
import org.carlspring.strongbox.services.TrustStoreService;

import javax.inject.Inject;
import java.net.InetAddress;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class TrustStoreServiceTestIT
{

    @Inject
    private TrustStoreService trustStoreService;

    private Resource trustStore;

    @Inject
    private KeyStoreManager keyStoreManager;

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;

    @BeforeEach
    public void before()
            throws Exception
    {
        InetAddress inetAddress = InetAddress.getByName("repository.apache.org");
        trustStore = getTrustStoreResource();
        keyStoreManager.removeCertificates(Paths.get(trustStore.getURI()), "password".toCharArray(), inetAddress, 443);
    }

    @Test
    public void shouldAddSslCertificatesToTrustStore()
            throws Exception
    {
        assertThat(keyStoreManager.listCertificates(Paths.get(trustStore.getURI()),
                                                     "password".toCharArray())
                                   .keySet()
                                   .stream()
                                   .anyMatch(name -> name.contains("*.apache.org")))
                .isFalse();

        trustStoreService.addSslCertificatesToTrustStore("https://repository.apache.org/snapshots/");

        assertThat(keyStoreManager.listCertificates(Paths.get(trustStore.getURI()),
                                                    "password".toCharArray())
                                  .keySet()
                                  .stream()
                                  .anyMatch(name -> name.contains("*.apache.org")))
                .isTrue();
    }

    private Resource getTrustStoreResource()
    {
        return configurationResourceResolver.getConfigurationResource("strongbox.truststore.jks",
                                                                      "etc/ssl/truststore.jks");
    }

}
