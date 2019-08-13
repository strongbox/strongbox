package org.carlspring.strongbox.controllers.ssl;

import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.ssl.HostForm;
import org.carlspring.strongbox.providers.repository.proxied.RestArtifactResolverFactory;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;

import javax.inject.Inject;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.InetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@Execution(SAME_THREAD)
class TrustStoreManagementControllerTestIT
        extends AbstractKeyStoreManagementControllerIT
{

    private static final String SELF_SIGNED_HOST = "self-signed.badssl.com";

    private static final String SELF_SIGNED_URL = "https://" + SELF_SIGNED_HOST;

    @Inject
    private RestArtifactResolverFactory restArtifactResolverFactory;

    @Override
    String getApiUrl()
    {
        return "/api/ssl/trustStore";
    }

    @Test()
    void shouldThrowExceptionWhenCallingToUntrustedRemote()
    {
        MutableRemoteRepository mutableRemoteRepository = new MutableRemoteRepository();
        mutableRemoteRepository.setUrl(SELF_SIGNED_URL);
        RemoteRepository remoteRepository = new RemoteRepository(mutableRemoteRepository);

        assertThatExceptionOfType(ProcessingException.class).isThrownBy(() -> {
            restArtifactResolverFactory.newInstance(remoteRepository).get("/");
        })
                                                            .withMessageContaining("PKIX path building failed")
                                                            .withCauseInstanceOf(SSLHandshakeException.class);
    }

    /**
     * TODO probably context refresh is required
     * @throws IOException
     */
    @Test
    void whenTheCertIsInTrustStoreConnectionShoulBePerformed()
            throws IOException
    {
        String url = getUrl();

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(new HostForm(InetAddress.getByName(SELF_SIGNED_HOST), 443))
                     .when()
                     .delete(url)
                     .then()
                     .statusCode(200);

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(new HostForm(InetAddress.getByName(SELF_SIGNED_HOST), 443))
                     .when()
                     .put(url)
                     .then()
                     .statusCode(200);

        // org.carlspring.strongbox.app.StrongboxSpringBootApplication.restart

        MutableRemoteRepository mutableRemoteRepository = new MutableRemoteRepository();
        mutableRemoteRepository.setUrl(SELF_SIGNED_URL);
        RemoteRepository remoteRepository = new RemoteRepository(mutableRemoteRepository);

        try (CloseableRestResponse closeableRestResponse = restArtifactResolverFactory.newInstance(
                remoteRepository).get("/"))
        {
            Response response = closeableRestResponse.getResponse();
            assertThat(response.getStatus()).isEqualTo(200);
        }

        // cleanup
        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(new HostForm(InetAddress.getByName(SELF_SIGNED_HOST), 443))
                     .when()
                     .delete(url)
                     .then()
                     .statusCode(200);

    }
}