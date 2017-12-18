package org.carlspring.strongbox.repository.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenMetadataGroupRepositoryComponentTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_LEAF_E = "leaf-repo-e";

    private static final String REPOSITORY_LEAF_L = "leaf-repo-l";

    private static final String REPOSITORY_LEAF_Z = "leaf-repo-Z";

    private static final String REPOSITORY_LEAF_D = "leaf-repo-d";

    private static final String REPOSITORY_LEAF_G = "leaf-repo-g";

    private static final String REPOSITORY_LEAF_K = "leaf-repo-k";

    private static final File REPOSITORY_LEAF_L_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                   "/storages/" + STORAGE0 + "/" +
                                                                   REPOSITORY_LEAF_L);

    private static final File REPOSITORY_LEAF_D_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                   "/storages/" + STORAGE0 + "/" +
                                                                   REPOSITORY_LEAF_D);

    private static final File REPOSITORY_LEAF_G_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                   "/storages/" + STORAGE0 + "/" +
                                                                   REPOSITORY_LEAF_G);

    private static final File REPOSITORY_LEAF_K_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                   "/storages/" + STORAGE0 + "/" +
                                                                   REPOSITORY_LEAF_K);

    private static final String REPOSITORY_GROUP_A = "group-repo-a";

    private static final String REPOSITORY_GROUP_B = "group-repo-b";

    private static final String REPOSITORY_GROUP_C = "group-repo-c";

    private static final String REPOSITORY_GROUP_F = "group-repo-f";

    private static final String REPOSITORY_GROUP_H = "group-repo-h";

    @Inject
    private MavenMetadataGroupRepositoryComponent mavenGroupRepositoryComponent;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_E));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_L));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_Z));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_G));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_C));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H));

        return repositories;
    }

    private Repository createRepository(String repositoryId)
            throws Exception
    {
        Repository repository = new Repository(repositoryId);
        repository.setStorage(configurationManager.getConfiguration().getStorage(STORAGE0));
        repository.setAllowsForceDeletion(true);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        createRepository(repository);
        return repository;
    }

    private void createLeaf(String repositoryId)
            throws Exception
    {
        Repository repository = createRepository(repositoryId);
        repository.setType(new Random().nextInt(2) % 2 == 0 ? RepositoryTypeEnum.HOSTED.getType() :
                           RepositoryTypeEnum.PROXY.getType());
    }

    private void createGroup(String repositoryId,
                             String... leafs)
            throws Exception
    {
        Repository repository = createRepository(repositoryId);
        repository.setType(RepositoryTypeEnum.GROUP.getType());
        repository.setGroupRepositories(new HashSet<>(Arrays.asList(leafs)));
    }

    private void copyArtifactMetadata(String sourceRepositoryId,
                                      String destinationRepositoryId,
                                      String path)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(sourceRepositoryId);
        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final Path sourcePath = layoutProvider.resolve(repository).resolve(path);

        repository = storage.getRepository(destinationRepositoryId);
        final Path destinationPath = layoutProvider.resolve(repository).resolve(path);
        FileUtils.copyFile(sourcePath.toFile(), destinationPath.toFile());
    }

    @Before
    public void initialize()
            throws Exception
    {
        createLeaf(REPOSITORY_LEAF_E);
        createLeaf(REPOSITORY_LEAF_L);
        createLeaf(REPOSITORY_LEAF_Z);
        createLeaf(REPOSITORY_LEAF_D);
        createLeaf(REPOSITORY_LEAF_G);
        createLeaf(REPOSITORY_LEAF_K);

        createGroup(REPOSITORY_GROUP_A, REPOSITORY_LEAF_G, REPOSITORY_GROUP_B);
        createGroup(REPOSITORY_GROUP_B, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);
        createGroup(REPOSITORY_GROUP_C, REPOSITORY_LEAF_E, REPOSITORY_LEAF_Z);
        createGroup(REPOSITORY_GROUP_F, REPOSITORY_GROUP_C, REPOSITORY_LEAF_D, REPOSITORY_LEAF_L);
        createGroup(REPOSITORY_GROUP_H, REPOSITORY_GROUP_F, REPOSITORY_LEAF_K);

        // whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible
        generateArtifact(REPOSITORY_LEAF_L_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        // whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible
        generateArtifact(REPOSITORY_LEAF_G_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_D_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_K_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_L);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_G);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_D);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_K);

        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_F, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_B, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_A, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_L, REPOSITORY_GROUP_H, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_K, REPOSITORY_GROUP_H, FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));

        /**
         <denied>
             <rule-set group-repository="group-repo-h">
                 <rule pattern=".*(com|org)/artifacts/to/update/releases/update-group.*">
                     <repositories>
                        <repository>leaf-repo-d</repository>
                     </repositories>
                 </rule>
             </rule-set>
         </denied>
         **/
        createRoutingRuleSet(STORAGE0,
                             REPOSITORY_GROUP_H,
                             new String[]{ REPOSITORY_LEAF_D },
                             ".*(com|org)/artifacts/to/update/releases/update-group.*",
                             ROUTING_RULE_TYPE_DENIED);
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible()
            throws Exception
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_LEAF_L);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        String path = "com/artifacts/to/delete/releases/delete-group/1.2.1/delete-group-1.2.1.jar";
        File artifactFile = new File(repository.getBasedir(), path);

        assertTrue("Failed to locate artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());

        layoutProvider.delete(STORAGE0, REPOSITORY_LEAF_L, path, false);
        ((Maven2LayoutProvider) layoutProvider).closeIndex(STORAGE0, REPOSITORY_LEAF_L, path);

        assertFalse("Failed to delete artifact file " + artifactFile.getAbsolutePath(), artifactFile.exists());


        // author of changes
        Metadata metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_L)).resolve(
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // direct parent
        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F)).resolve(
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // next direct parent
        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B)).resolve(
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // grand parent
        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H)).resolve(
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.2"));

        // grand parent with other kids
        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A)).resolve(
                        "com/artifacts/to/delete/releases/delete-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

    @Test
    public void whenMetadataWasUploadedInRepositoryAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible()
            throws Exception
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_LEAF_D);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        Metadata metadata;

        // BEFORE
        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_F)).resolve(
                            "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B)).resolve(
                            "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A)).resolve(
                            "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        // IMITATE THE EVENT
        mavenGroupRepositoryComponent.updateMetadataInGroupsContainingRepository(STORAGE0, REPOSITORY_LEAF_D,
                                                                                 "com/artifacts/to/update/releases/update-group/maven-metadata.xml",
                                                                                 Path::getParent);

        // AFTER
        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_D)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_K)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_H)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_B)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                layoutProvider.resolve(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_A)).resolve(
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

}
