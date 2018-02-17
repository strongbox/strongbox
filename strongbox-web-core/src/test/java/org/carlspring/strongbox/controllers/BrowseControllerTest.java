package org.carlspring.strongbox.controllers;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.DirectoryContent;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Guido Grazioli
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class BrowseControllerTest
        extends RestAssuredBaseTest
{

    @BeforeClass
    public static void setup()
            throws Exception
    {
        Files.createFile(Paths.get("target/strongbox-vault/storages/storage0/releases","testfile"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","testdir"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","testdir/testsubdir"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","org1"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","org1/groupdir"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","org1/groupdir/com"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","org1/groupdir/com/artifactdir"))
             .toFile()
             .deleteOnExit();
        Files.createDirectory(Paths.get("target/strongbox-vault/storages/storage0/releases","org1/groupdir/com/artifactdir/1.0.0"))
             .toFile()
             .deleteOnExit();
        Files.createFile(Paths.get("target/strongbox-vault/storages/storage0/releases","org1/groupdir/com/artifactdir/1.0.0/artifactdir-1.0.0.jar"))
             .toFile()
             .deleteOnExit();
    }
    
    @Test
    public void testGetStorages()
            throws Exception
    {

        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT;
        String storages = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                 .when()
                                 .get(url)
                                 .prettyPeek()
                                 .asString();
        
        DirectoryContent returned = new ObjectMapper()
                .readValue(storages, DirectoryContent.class);
              
        assertNotNull("Failed to get storage list!", returned);
        assertNotNull("Failed to get storage list!", returned.getDirectories());
        assertFalse("Returned storage size does not match", returned.getDirectories().isEmpty());      
    }

    @Test
    public void testGetRepositories()
            throws Exception
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0";
        String repos = given().accept(MediaType.APPLICATION_JSON_VALUE)
                              .when()
                              .get(url)
                              .prettyPeek()
                              .asString();

        DirectoryContent returned = new ObjectMapper()
                .readValue(repos, DirectoryContent.class);
        
        assertNotNull("Failed to get repository list!", returned);
        assertNotNull("Failed to get repository list!", returned.getDirectories());
        assertTrue("Returned repositories do not match", !returned.getDirectories().isEmpty());
        assertTrue("Repository not found", returned.getDirectories()
                                                   .stream()
                                                   .filter(p -> p.getName().equals("releases"))
                                                   .findFirst()
                                                   .isPresent());
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
    }

    @Ignore
    @Test
    public void testRepositoryContents()
            throws Exception
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/releases";
        String contents = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                 .when()
                                 .get(url)
                                 .prettyPeek()
                                 .asString();
        
        DirectoryContent returned = new ObjectMapper()
                .readValue(contents, DirectoryContent.class);
        
        assertTrue("Invalid files returned", returned.getFiles().size() == 1
                        && returned.getDirectories().get(0).getName().equals("testfile"));                                                           
        assertTrue("Invalid files returned", returned.getDirectories().size() == 1
                    && returned.getDirectories().get(0).getName().equals("testdir"));
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
    }
    
    @Test
    public void testRepositoryContentsWithPath()
            throws Exception
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/releases/testdir";
        String contents = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                 .when()
                                 .get(url)
                                 .prettyPeek()
                                 .asString();

        DirectoryContent returned = new ObjectMapper()
                .readValue(contents, DirectoryContent.class);
        
        assertTrue("Invalid files returned", returned.getFiles().isEmpty());
        assertTrue("Invalid files returned", returned.getDirectories().size() == 1
                && returned.getDirectories().get(0).getName().equals("testsubdir"));
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
    
    @Test
    public void testBrowseRepositoryContents()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/releases/org1/groupdir/com/artifactdir/1.0.0/";
        String responseBody = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .then()
                                     .statusCode(200)
                                     .and()
                                     .extract()
                                     .asString();
               
        assertTrue("Returned HTML is incorrect", responseBody.contains("/storages/storage0/releases/org1/groupdir/com/artifactdir/1.0.0/artifactdir-1.0.0.jar'"));
    }
    
}
