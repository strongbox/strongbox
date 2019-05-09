package org.carlspring.strongbox.security;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(response.isCommitted());
        assertThat(response.getStatus(), CoreMatchers.equalTo(HttpServletResponse.SC_FORBIDDEN));
        assertThat(response.getContentType(), CoreMatchers.equalTo(MediaType.APPLICATION_JSON_VALUE));
        assertThat(responseEntityBody.getError(), CoreMatchers.equalTo("forbidden"));
    }

    @Test
    @WithMockUser(username = "unauthorizedUser")
    public void unauthorizedUserShouldReceiveExpectedUnauthorizedResponse()
    {
        given().contentType("application/json")
               .accept(ContentType.JSON)
               .when()
               .get("/api/configuration/strongbox")
               .peek()
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .contentType("application/json")
               .body("error", CoreMatchers.equalTo("forbidden"));
    }

}
