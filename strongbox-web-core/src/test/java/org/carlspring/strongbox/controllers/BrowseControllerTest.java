package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guido Grazioli
 */

@IntegrationTest
@ExtendWith(SpringExtension.class)
public class BrowseControllerTest
        extends MavenRestAssuredBaseTest
{
    
    private static final Logger logger = LoggerFactory.getLogger(BrowseControllerTest.class);
    
    private static final String REPOSITORY = "browsing-test-repository";
    
    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;

    @BeforeAll
    public static void setup()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }
    
    private static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY, Maven2LayoutProvider.ALIAS));
        return repositories;        
    }
    
    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        File GENERATOR_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/local");

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        MutableRepository repository = mavenRepositoryFactory.createRepository(REPOSITORY);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repository);
        
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY).getAbsolutePath(),
                         "org.carlspring.strongbox.browsing:test-browsing",
                         new String[]{ "1.1",
                                       "3.2"  
                         }
        );      
    } 
    
    @Override
    @AfterEach
    public void shutdown()
    {
        try
        {
            closeIndexersForRepository(STORAGE0, REPOSITORY);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        super.shutdown();
    }
    
    @Test
    public void testGetStorages()
            throws Exception
    {

        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT;
        
        String jsonResponse = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .asString();

        DirectoryListing returned = new ObjectMapper().readValue(jsonResponse, DirectoryListing.class);
              
        assertNotNull(returned, "Failed to get storage list!");
        assertNotNull(returned.getDirectories(), "Failed to get storage list!");
        assertFalse(returned.getDirectories().isEmpty(), "Returned storage size does not match");
        
        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/")
                                     .prettyPeek()
                                     .then()
                                     .statusCode(200)
                                     .and()
                                     .extract()
                                     .asString();
       
        assertTrue(htmlResponse.contains("storage0"), "Returned HTML is incorrect");
    }

    @Test
    public void testGetRepositories()
            throws Exception
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/" + STORAGE0;
        
        String jsonResponse = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .asString();

        DirectoryListing returned = new ObjectMapper().readValue(jsonResponse, DirectoryListing.class);
        
        assertNotNull(returned, "Failed to get repository list!");
        assertNotNull(returned.getDirectories(), "Failed to get repository list!");
        assertFalse(returned.getDirectories().isEmpty(), "Returned repositories do not match");
        assertTrue(returned.getDirectories()
                           .stream()
                           .anyMatch(p -> p.getName().equals(REPOSITORY)), "Repository not found");
        
        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/")
                                     .prettyPeek()
                                     .then()
                                     .statusCode(200)
                                     .and()
                                     .extract()
                                     .asString();

        assertTrue(htmlResponse.contains(REPOSITORY), "Returned HTML is incorrect");
}
                                 


    @Test
    public void testGetRepositoriesWithStorageNotFound()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storagefoo";
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(404);
        
        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(404);
    }

    @Test
    public void testRepositoryContents()
            throws Exception
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/" + STORAGE0 + "/" + REPOSITORY
                     + "/org/carlspring/strongbox/browsing/test-browsing/1.1";
        
        String jsonResponse = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .asString();
        
        DirectoryListing returned = new ObjectMapper().readValue(jsonResponse, DirectoryListing.class);
        
        assertTrue(returned.getFiles().size() == 6
                   && returned.getFiles().get(0).getName().equals("test-browsing-1.1.jar"), "Invalid files returned");
    
        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/")
                                     .prettyPeek()
                                     .asString();

        String link = getContextBaseUrl() + "/storages/"
                      + STORAGE0 + "/" + REPOSITORY + "/org/carlspring/strongbox/browsing/test-browsing/1.1/test-browsing-1.1.jar";

        assertTrue(htmlResponse.contains(link), "Expected to have found [ " + link + " ] in the response html");
    }

    @Test
    public void testRepositoryContentsWithRepositoryNotFound()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/repofoo";
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(404);   
                
        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(404);   
    }
  
    @Test
    public void testRepositoryContentsWithPathNotFound()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/releases/foo/bar";
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(404);
       
        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(404);        
    }
}
