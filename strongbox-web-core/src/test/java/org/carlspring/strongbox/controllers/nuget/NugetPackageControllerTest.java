package org.carlspring.strongbox.controllers.nuget;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

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
    public void test()
        throws Exception
    {
        String basedir = PropertyUtils.getHomeDirectory() + "/tmp";

        String packageId = "Org.Carlspring.Strongbox.Examples.Nuget.Mono";
        String packageVersion = "1.0.0";
        String packageFileName = packageId + "." + packageVersion + ".nupkg";

        NugetPackageGenerator nugetPackageGenerator = new NugetPackageGenerator(basedir);
        nugetPackageGenerator.generateNugetPackage(packageId, packageVersion);

        // client.put(Files.newInputStream(Paths.get(basedir).resolve(packageVersion).resolve(packageFileName)),
        // getContextBaseUrl() + "/storages/" + STORAGE_ID + "/" + REPOSITORY_RELEASES_1 + "/", packageFileName,
        // ContentType.BINARY.toString());

        given().header("User-Agent", "NuGet/*")
               .multiPart("file", packageFileName,
                          Files.readAllBytes(Paths.get(basedir).resolve(packageVersion).resolve(packageFileName)))
               .when()
               .post(getContextBaseUrl() + "/storages/" + STORAGE_ID + "/" + REPOSITORY_RELEASES_1 + "/")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

    }

}
