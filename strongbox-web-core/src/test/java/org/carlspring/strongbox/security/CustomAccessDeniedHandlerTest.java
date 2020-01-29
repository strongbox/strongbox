package org.carlspring.strongbox.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
public class CustomAccessDeniedHandlerTest
        extends RestAssuredBaseTest
{

    @Inject
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    public void customAccessDeniedHandlerShouldRespondAsExpected()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        customAccessDeniedHandler.handle(request, response, new AccessDeniedException("access denied"));
        ErrorResponseEntityBody responseEntityBody = objectMapper.readValue(response.getContentAsByteArray(),
                                                                            ErrorResponseEntityBody.class);

        assertThat(response.isCommitted()).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(responseEntityBody.getError()).isEqualTo("forbidden");
    }

    @Test
    @WithMockUser(username = "unauthorizedUser")
    public void unauthorizedUserShouldReceiveExpectedUnauthorizedResponse()
    {
        mockMvc.contentType(ContentType.JSON)
               .accept(ContentType.JSON)
               .when()
               .get("/api/configuration/strongbox")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .contentType(ContentType.JSON)
               .body("error", equalTo("forbidden"));
    }

}
