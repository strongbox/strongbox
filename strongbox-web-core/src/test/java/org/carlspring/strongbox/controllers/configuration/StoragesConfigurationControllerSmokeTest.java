package org.carlspring.strongbox.controllers.configuration;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.StoragesConfigurationController.SUCCESSFUL_REPOSITORY_SAVE;
import static org.carlspring.strongbox.controllers.configuration.StoragesConfigurationController.SUCCESSFUL_SAVE_STORAGE;
import static org.carlspring.strongbox.controllers.configuration.StoragesConfigurationController.SUCCESSFUL_STORAGE_REMOVAL;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.forms.configuration.StorageForm;
import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryStatusEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@IntegrationTest
public class StoragesConfigurationControllerSmokeTest extends RestAssuredBaseTest
{

    @Inject
    private PropertiesBooter propertiesBooter;
    
    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();

        setContextBaseUrl("/api/configuration/strongbox/storages");
    }

    @Test
    public void artifactShouldBeDeployedIntoNewRepository() throws IOException
    {
        String storageId = "storage-sccst";
        String repositoryId = "sccst-releases";
        Path artifactPath = Paths.get(propertiesBooter.getVaultDirectory())
                                 .resolve("storages")
                                 .resolve(storageId)
                                 .resolve(repositoryId)
                                 .resolve("dummy-file.zip");
        assertFalse(Files.exists(artifactPath));
        
        StorageForm storageForm = new StorageForm();
        storageForm.setId(storageId);

        // 1. Create storage
        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storageForm)
                     .when()
                     .put(getContextBaseUrl())
                     .prettyPeek()
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_SAVE_STORAGE));

        // 2. Create repository
        RepositoryForm repositoryForm = new RepositoryForm();
        repositoryForm.setId(repositoryId);
        repositoryForm.setLayout(RawLayoutProvider.ALIAS);
        repositoryForm.setImplementation(StorageProviderEnum.FILESYSTEM.describe());
        repositoryForm.setStatus(RepositoryStatusEnum.IN_SERVICE.describe());
        repositoryForm.setType(RepositoryTypeEnum.HOSTED.describe());
        repositoryForm.setPolicy(RepositoryPolicyEnum.RELEASE.describe());

        
        
        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(repositoryForm)
                     .when()
                     .put(getContextBaseUrl()  + "/" + storageForm.getId() + "/" + repositoryForm.getId())
                     .prettyPeek()
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_REPOSITORY_SAVE));

        // 3. Deploy artifact
        given().header("user-agent", "Raw/*")
               .body(createZipFile())
               .when()
               .put(String.format("/storages/%s/%s/dummy-file.zip", storageForm.getId(), repositoryForm.getId()))
               .then()
               .statusCode(HttpStatus.OK.value());
        
        assertTrue(Files.exists(artifactPath));
        
        // 4. Delete storage.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .accept(MediaType.TEXT_PLAIN_VALUE)
                     .param("force", true)
                     .when()
                     .delete(getContextBaseUrl() + "/" + storageForm.getId())
                     .prettyPeek()
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_STORAGE_REMOVAL));
        
        assertFalse(Files.exists(artifactPath));
    }

    private byte[] createZipFile()
            throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(os))
        {
            ZipEntry entry = new ZipEntry("dummy-file.txt");

            zos.putNextEntry(entry);
            zos.write("this is a test file".getBytes());
            zos.closeEntry();
        }

        return os.toByteArray();
    }

}
