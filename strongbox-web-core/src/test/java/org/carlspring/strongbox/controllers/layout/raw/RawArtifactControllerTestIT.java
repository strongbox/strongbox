package org.carlspring.strongbox.controllers.layout.raw;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RawRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    private static final String REPOSITORY_GROUP = "ractit-raw-group";

    @Inject
    RawRepositoryFactory rawRepositoryFactory;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, RawLayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY, RawLayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    public void init()
            throws Exception
    {
        super.init();

        MutableRepository repository1 = rawRepositoryFactory.createRepository(REPOSITORY_RELEASES);
        repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());

        createRepository(repository1, STORAGE0);

        //noinspection ResultOfMethodCallIgnored
        Files.createDirectories(Paths.get(TEST_RESOURCES));

        createFile(new Repository(repository1), "org/foo/bar/blah.zip");

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://www-eu.apache.org/dist");
        // Required for http://www-eu.apache.org/dist/maven/pom/apache-19-source-release.zip

        MutableRepository repository2 = rawRepositoryFactory.createRepository(REPOSITORY_GROUP);
        repository2.setType(RepositoryTypeEnum.GROUP.getType());
        repository2.setGroupRepositories(Sets.newHashSet(STORAGE0 + ":" + REPOSITORY_PROXY));

        createRepository(repository2, STORAGE0);
        // Required for apache-19-source-release.zip
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
                              "/maven/pom/apache-19-source-release.zip";

        resolveArtifact(artifactPath);
    }

    /**
     * Note: This test requires an internet connection.
     *
     * @throws Exception
     */
    @Test
    public void testResolveArtifactViaGroupWithProxy()
            throws Exception
    {
        String artifactPath = "/storages/" + STORAGE0 + "/" + REPOSITORY_GROUP +
                              "/maven/pom/maven-parent-31-source-release.zip";

        resolveArtifact(artifactPath);
    }

}
