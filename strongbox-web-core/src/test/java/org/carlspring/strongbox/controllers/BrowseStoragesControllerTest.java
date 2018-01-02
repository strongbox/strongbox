package org.carlspring.strongbox.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecBuilder;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ExtractableResponse;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithRepository;
import org.carlspring.strongbox.web.HeaderMappingFilter;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author sanket407
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class BrowseStoragesControllerTest
        extends TestCaseWithRepository
{
    
    
    @Test
    public void testBrowseStorages()
            throws Exception
    {
        String path = "/storages/";

        RestAssuredMockMvc.given()
                          .header("User-Agent", "unknown/*")
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(path)
                          .then()
                          .statusCode(302);
    }

    @Test
    public void testBrowseRepositoriesInStorages()
            throws Exception
    {
        String path = "/storages/storage0/";

        RestAssuredMockMvc.given()
                          .header("User-Agent", "unknown/*")
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(path)
                          .then()
                          .statusCode(302);
    }
    
}
