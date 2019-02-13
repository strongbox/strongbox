package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guido Grazioli
 * @author Pablo Tirado
 */
@IntegrationTest
@SpringBootTest
public class BrowseControllerTest
        extends MavenRestAssuredBaseTest
{

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

        setContextBaseUrl(BrowseController.ROOT_CONTEXT);

        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

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
            removeRepositories();
            cleanUp(getRepositoriesToClean());
        }
        catch (Exception e)
        {
            throw new UndeclaredThrowableException(e);
        }
        super.shutdown();
    }

    private void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }
    
    @Test
    public void testGetStorages()
            throws Exception
    {

        String url = getContextBaseUrl();
        
        DirectoryListing returned = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                               .when()
                                               .get(url)
                                               .prettyPeek()
                                               .as(DirectoryListing.class);

        assertNotNull(returned, "Failed to get storage list!");
        assertNotNull(returned.getDirectories(), "Failed to get storage list!");
        assertFalse(returned.getDirectories().isEmpty(), "Returned storage size does not match");

        List<FileContent> expectedSortedList = returned.getDirectories()
                                                       .stream()
                                                       .sorted(Comparator.comparing(FileContent::getName))
                                                       .collect(Collectors.toList());

        assertEquals(expectedSortedList, returned.getDirectories(), "Returned storages are not sorted!");

        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/")
                                     .prettyPeek()
                                     .then()
                                     .statusCode(OK)
                                     .and()
                                     .extract()
                                     .asString();
       
        assertTrue(htmlResponse.contains(STORAGE0), "Returned HTML is incorrect");
    }

    @Test
    public void testGetRepositories()
            throws Exception
    {
        String url = getContextBaseUrl() + "/" + STORAGE0;

        DirectoryListing returned = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .as(DirectoryListing.class);

        assertNotNull(returned, "Failed to get repository list!");
        assertNotNull(returned.getDirectories(), "Failed to get repository list!");
        assertFalse(returned.getDirectories().isEmpty(), "Returned repositories do not match");
        assertTrue(returned.getDirectories()
                           .stream()
                           .anyMatch(p -> p.getName().equals(REPOSITORY)), "Repository not found");

        List<FileContent> expectedSortedList = returned.getDirectories()
                                                       .stream()
                                                       .sorted(Comparator.comparing(FileContent::getName))
                                                       .collect(Collectors.toList());

        assertEquals(expectedSortedList, returned.getDirectories(), "Returned repositories are not sorted!");
        
        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/")
                                     .prettyPeek()
                                     .then()
                                     .statusCode(OK)
                                     .and()
                                     .extract()
                                     .asString();

        assertTrue(htmlResponse.contains(REPOSITORY), "Returned HTML is incorrect");
}
                                 


    @Test
    public void testGetRepositoriesWithStorageNotFound()
    {
        String url = getContextBaseUrl() + "/storagefoo";
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
        
        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testRepositoryContents()
            throws Exception
    {
        String url = getContextBaseUrl() + "/" + STORAGE0 + "/" + REPOSITORY
                     + "/org/carlspring/strongbox/browsing/test-browsing/1.1";

        DirectoryListing returned = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .as(DirectoryListing.class);

        assertTrue(returned.getFiles().size() == 6
                   && returned.getFiles().get(0).getName().equals("test-browsing-1.1.jar"), "Invalid files returned");
    
        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/")
                                     .prettyPeek()
                                     .asString();

        String link = "/storages/" + STORAGE0 + "/" + REPOSITORY +
                      "/org/carlspring/strongbox/browsing/test-browsing/1.1/test-browsing-1.1.jar";

        assertTrue(htmlResponse.contains(link), "Expected to have found [ " + link + " ] in the response html");
    }

    @Test
    public void testRepositoryContentsWithRepositoryNotFound()
    {
        String url = getContextBaseUrl() + "/" + STORAGE0 + "/repofoo";
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());   
                
        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());   
    }
  
    @Test
    public void testRepositoryContentsWithPathNotFound()
    {
        String url = getContextBaseUrl() + "/" + STORAGE0  + "/releases/foo/bar";
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
       
        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());        
    }
}
