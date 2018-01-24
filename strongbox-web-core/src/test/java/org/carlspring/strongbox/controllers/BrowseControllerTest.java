package org.carlspring.strongbox.controllers;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.carlspring.strongbox.config.IntegrationTest;
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
        String storages = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                 .when()
                                 .get(url)
                                 .prettyPeek()
                                 .asString();

        Map<String, List<String>> returnedMap = new ObjectMapper()
                .readValue(storages, new TypeReference<Map<String, List<String>>>(){});
        
        assertNotNull("Failed to get storage list!", returnedMap);
        assertNotNull("Failed to get storage list!", returnedMap.get("storages"));
        assertFalse("Returned storage size does not match", returnedMap.get("storages").isEmpty());
    }

    @Test
    public void testGetRepositories()
            throws Exception
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0";
        String repos = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                              .when()
                              .get(url)
                              .prettyPeek()
                              .asString();

        Map<String, List<String>> returnedMap = new ObjectMapper()
                .readValue(repos, new TypeReference<Map<String, List<String>>>(){});
        
        assertNotNull("Failed to get repository list!", returnedMap);
        assertNotNull("Failed to get repository list!", returnedMap.get("repositories"));
        assertTrue("Returned repositories do not match", !returnedMap.get("repositories").isEmpty());
        assertTrue("Returned repos do not match", returnedMap.get("repositories").contains("releases"));
    }

    @Test
    public void testGetRepositoriesWithStorageNotFound()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storagefoo";
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        String contents = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                 .when()
                                 .get(url)
                                 .prettyPeek()
                                 .asString();
        
        Map<String, List<String>> returnedMap = new ObjectMapper()
                .readValue(contents, new TypeReference<Map<String, List<String>>>(){});
        
        assertArrayEquals("Returned files", new String[] { "testfile" }, returnedMap.get("files").toArray());
        assertArrayEquals("Returned files", new String[] { "testdir" }, returnedMap.get("directories").toArray());
    }

    @Test
    public void testRepositoryContentsWithRepositoryNotFound()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/repofoo";
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        String contents = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                 .when()
                                 .get(url)
                                 .prettyPeek()
                                 .asString();

        Map<String, List<String>> returnedMap = new ObjectMapper()
                .readValue(contents, new TypeReference<Map<String, List<String>>>(){});
        
        assertArrayEquals("Returned files", new String[] { }, returnedMap.get("files").toArray());
        assertArrayEquals("Returned files", new String[] { "testsubdir" }, returnedMap.get("directories").toArray());
    }

    @Test
    public void testRepositoryContentsWithPathNotFound()
    {
        String url = getContextBaseUrl() + BrowseController.ROOT_CONTEXT + "/storage0/releases/foo/bar";
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
                                     .statusCode(302)
                                     .and()
                                     .extract()
                                     .asString();
               
        assertTrue("Returned HTML is incorrect", responseBody.contains("/storages/storage0/releases/org1/groupdir/com/artifactdir/1.0.0/artifactdir-1.0.0.jar'"));
               
        
        
        
    }
    
}
