package org.carlspring.strongbox.security.certificates;

import org.carlspring.strongbox.SecurityApiTestConfig;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = SecurityApiTestConfig.class)
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(SAME_THREAD)
class TrustStoreManagerTestIT
{

    @Inject
    private TrustStoreManager trustStoreManager;

    private InetAddress inetAddress;

    @BeforeEach
    public void before()
            throws Exception
    {
        inetAddress = InetAddress.getByName("repository.apache.org");
        trustStoreManager.removeCertificates(inetAddress, 443);
    }

    @Test
    public void shouldAddSslCertificatesToTrustStore()
            throws Exception
    {
        assertExistCertificate("*.apache.org", false);

        trustStoreManager.addCertificates(inetAddress, 443);

        assertExistCertificate("*.apache.org", true);
    }

    @Test
    public void shouldRemoveSslCertificateFromTrustStore()
            throws Exception
    {
        assertExistCertificate("*.apache.org", false);

        trustStoreManager.addCertificates(inetAddress, 443);

        assertExistCertificate("*.apache.org", true);

        trustStoreManager.removeCertificates(inetAddress, 443);

        assertExistCertificate("*.apache.org", false);
    }

    @Test
    public void shouldListCertificates()
            throws Exception
    {
        Map<String, Certificate> certificates = trustStoreManager.listCertificates();

        org.assertj.core.api.Assertions.assertThat(certificates).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(certificates.get("localhost")).isNotNull();

        assertExistCertificate("*.apache.org", false);

        trustStoreManager.addCertificates(inetAddress, 443);

        assertExistCertificate("*.apache.org", true);

        certificates = trustStoreManager.listCertificates();

        org.assertj.core.api.Assertions.assertThat(certificates).hasSize(4);
        org.assertj.core.api.Assertions.assertThat(certificates.get("localhost")).isNotNull();
        org.assertj.core.api.Assertions.assertThat(
                certificates.keySet().stream().filter(s -> s.startsWith("207.244.88.140:443")).collect(
                        Collectors.toList())).hasSize(3);
    }

    private void assertExistCertificate(String namePart,
                                        boolean shouldExist)
            throws GeneralSecurityException, IOException
    {
        Consumer<Boolean> assertion = shouldExist ? Assertions::assertTrue : Assertions::assertFalse;

        assertion.accept(trustStoreManager.listCertificates()
                                          .keySet()
                                          .stream()
                                          .filter(name -> name.contains(namePart)).findAny().isPresent());
    }

}