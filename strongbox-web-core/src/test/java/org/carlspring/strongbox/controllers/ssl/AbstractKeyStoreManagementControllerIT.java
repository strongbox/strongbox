package org.carlspring.strongbox.controllers.ssl;

import org.carlspring.strongbox.forms.ssl.HostForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 */
abstract class AbstractKeyStoreManagementControllerIT
        extends RestAssuredBaseTest
{

    abstract String getApiUrl();

    protected String getUrl()
    {
        return getContextBaseUrl() + getApiUrl();
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    void shouldAddListAndRemoveCertificateWithoutUsingProxy()
            throws Exception
    {
        String url = getUrl();

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(new HostForm(InetAddress.getByName("google.com"), 443))
                     .when()
                     .put(url)
                     .then()
                     .statusCode(200);

        Set<String> as = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                .when()
                                .get(url + "/aliases")
                                .peek()
                                .then()
                                .statusCode(200)
                                .extract()
                                .as(Set.class);

        assertThat(as).hasSize(3);
        Iterator<String> iterator = as.iterator();
        assertThat(iterator.next()).isEqualTo("localhost");
        assertThat(iterator.next()).contains("google");
        assertThat(iterator.next()).contains("google");

        Integer size = given().accept(MediaType.APPLICATION_JSON_VALUE)
                              .when()
                              .get(url + "/size")
                              .peek()
                              .then()
                              .statusCode(200)
                              .extract()
                              .as(Integer.class);

        assertThat(size).isEqualTo(3);

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(new HostForm(InetAddress.getByName("google.com"), 443))
                     .when()
                     .delete(url)
                     .then()
                     .statusCode(200);

        as = given().accept(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .get(url + "/aliases")
                    .peek()
                    .then()
                    .statusCode(200)
                    .extract()
                    .as(Set.class);

        assertThat(as).hasSize(1);
        iterator = as.iterator();
        assertThat(iterator.next()).isEqualTo("localhost");

        size = given().accept(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .get(url + "/size")
                      .peek()
                      .then()
                      .statusCode(200)
                      .extract()
                      .as(Integer.class);

        assertThat(size).isEqualTo(1);
    }


}
