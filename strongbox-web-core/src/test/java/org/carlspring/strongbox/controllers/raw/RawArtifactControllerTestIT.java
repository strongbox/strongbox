package org.carlspring.strongbox.controllers.raw;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.*;

/**
 * @author Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RawArtifactControllerTestIT
        extends RawRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "ractit-raw-releases";

    private static final String REPOSITORY_PROXY = "ractit-raw-proxy";


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, RawLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY, RawLayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        Repository repository = new Repository(REPOSITORY_RELEASES);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository.setStorage(getConfiguration().getStorage(STORAGE0));
        repository.setLayout(RawLayoutProvider.ALIAS);

        createRepository(repository);

        //noinspection ResultOfMethodCallIgnored
        Files.createDirectories(Paths.get(TEST_RESOURCES));

        createFile(repository, "org/foo/bar/blah.zip");

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://www-eu.apache.org/dist");
        // Required for http://www-eu.apache.org/dist/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz.sha1
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    /**
     * Note: This test requires an internet connection.
     *
     * @throws Exception
     */
    @Test
    public void testResolveArtifactViaProxy()
            throws Exception
    {
        String artifactPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_PROXY +
                              "/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz.sha1";

        resolveArtifact(artifactPath);
    }

}
