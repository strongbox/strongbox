package org.carlspring.strongbox.controllers.nuget;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.carlspring.strongbox.artifact.generator.NugetPackageGenerator;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.data.PropertyUtils;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Throwables;

import io.restassured.config.MultiPartConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.matcher.ResponseAwareMatcherComposer;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;

@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class NugetPackageControllerTest extends RestAssuredBaseTest
{

    private final static String STORAGE_ID = "storage-nuget-test";

    private static final String REPOSITORY_RELEASES_1 = "nuget-releases-1";

    @BeforeClass
    public static void cleanUp()
        throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_1));

        return repositories;
    }

    @Override
    public void init()
        throws Exception
    {
        super.init();

        createStorage(STORAGE_ID);

        Repository repository1 = new Repository(REPOSITORY_RELEASES_1);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository1.setStorage(configurationManager.getConfiguration()
                                                   .getStorage(STORAGE_ID));
        repository1.setLayout("Nuget Hierarchical");
        repository1.setIndexingEnabled(false);

        createRepository(repository1);
    }

    @Override
    public void shutdown()
    {
        try
        {
            getRepositoryIndexManager().closeIndexersForRepository(STORAGE_ID, REPOSITORY_RELEASES_1);
        }
        catch (IOException e)
        {
            throw Throwables.propagate(e);
        }
        super.shutdown();
    }

    @Test
    public void testPackageCommonFlow()
        throws Exception
    {
        String basedir = PropertyUtils.getHomeDirectory() + "/tmp";

        String packageId = "Org.Carlspring.Strongbox.Examples.Nuget.Mono";
        String packageVersion = "1.0.0";
        String packageFileName = packageId + "." + packageVersion + ".nupkg";
        Path packageFilePath = Paths.get(basedir).resolve(packageVersion).resolve(packageFileName);
        long packageFileSize = Files.size(packageFilePath);

        NugetPackageGenerator nugetPackageGenerator = new NugetPackageGenerator(basedir);
        nugetPackageGenerator.generateNugetPackage(packageId, packageVersion);

        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

        MultipartEntityBuilder.create()
                              .addBinaryBody("package",
                                             Files.newInputStream(packageFilePath))
                              .build()
                              .writeTo(contentStream);

        given().header("User-Agent", "NuGet/*")
               .header("X-NuGet-ApiKey",
                       "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdHJvbmdib3giLCJqdGkiOiJ0SExSbWU4eFJOSnJjNXVXdTVkZDhRIiwic3ViIjoiYWRtaW4iLCJzZWN1cml0eS10b2tlbi1rZXkiOiJhZG1pbi1zZWNyZXQifQ.xRWxXt5yob5qcHjsvV1YsyfY3C-XFt9oKPABY0tYx88")
               .header("Content-Type", "multipart/form-data; boundary=---------------------------123qwe")
               .body(contentStream.toByteArray())
               .when()
               .put(getContextBaseUrl() + "/storages/" + STORAGE_ID + "/" + REPOSITORY_RELEASES_1 + "/")
               .peek()
               .then()
               .statusCode(HttpStatus.CREATED.value());

        given().header("User-Agent", "NuGet/*")
               .when()
               .get(getContextBaseUrl() + "/storages/" + STORAGE_ID + "/" + REPOSITORY_RELEASES_1 + "/download/"
                       + packageId + "/" + packageVersion)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .assertThat()
               .header("Content-Length", equalTo(String.valueOf(packageFileSize)));

    }

}
