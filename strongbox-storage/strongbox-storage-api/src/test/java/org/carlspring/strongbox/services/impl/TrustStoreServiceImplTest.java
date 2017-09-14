package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.security.certificates.KeyStoreManager;
import org.carlspring.strongbox.services.TrustStoreService;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.net.InetAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = StorageApiTestConfig.class)
public class TrustStoreServiceImplTest
{

    @Inject
    private TrustStoreService trustStoreService;

    @Value("classpath:etc/ssl/truststore.jks")
    private Resource trustStore;

    @Inject
    private KeyStoreManager keyStoreManager;

    private InetAddress inetAddress;

    @Before
    public void before()
            throws Exception
    {
        inetAddress = InetAddress.getByName("repository.apache.org");
        keyStoreManager.removeCertificates(trustStore.getFile(), "password".toCharArray(), inetAddress, 443);

        Field trustStoreField = TrustStoreServiceImpl.class.getDeclaredField("trustStore");
        ReflectionUtils.makeAccessible(trustStoreField);
        ReflectionUtils.setField(trustStoreField, trustStoreService, trustStore);
    }

    @Test
    public void shouldAddSslCertificatesToTrustStore()
            throws Exception
    {
        Assert.assertFalse(keyStoreManager.listCertificates(trustStore.getFile(),
                                                            "password".toCharArray()).keySet().stream().filter(
                name -> name.contains("*.apache.org")).findAny().isPresent());

        trustStoreService.addSslCertificatesToTrustStore("https://repository.apache.org/snapshots/");

        Assert.assertTrue(keyStoreManager.listCertificates(trustStore.getFile(),
                                                           "password".toCharArray()).keySet().stream().filter(
                name -> name.contains("*.apache.org")).findAny().isPresent());
    }

}