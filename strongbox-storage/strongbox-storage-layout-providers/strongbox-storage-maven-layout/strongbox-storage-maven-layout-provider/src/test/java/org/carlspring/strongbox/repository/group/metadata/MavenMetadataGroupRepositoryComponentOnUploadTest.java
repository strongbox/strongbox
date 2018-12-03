package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenMetadataGroupRepositoryComponentOnUploadTest
        extends BaseMavenGroupRepositoryComponentTest
{

    private static final String REPOSITORY_LEAF_AE = "leaf-repo-ae";

    private static final String REPOSITORY_LEAF_AL = "leaf-repo-al";

    private static final String REPOSITORY_LEAF_AZ = "leaf-repo-az";

    private static final String REPOSITORY_LEAF_AD = "leaf-repo-ad";

    private static final String REPOSITORY_LEAF_AG = "leaf-repo-ag";

    private static final String REPOSITORY_LEAF_AK = "leaf-repo-ak";

    private static final File REPOSITORY_LEAF_AL_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                    "/storages/" + STORAGE0 + "/" +
                                                                    REPOSITORY_LEAF_AL);

    private static final File REPOSITORY_LEAF_AD_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                    "/storages/" + STORAGE0 + "/" +
                                                                    REPOSITORY_LEAF_AD);

    private static final File REPOSITORY_LEAF_AG_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                    "/storages/" + STORAGE0 + "/" +
                                                                    REPOSITORY_LEAF_AG);

    private static final File REPOSITORY_LEAF_AK_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                    "/storages/" + STORAGE0 + "/" +
                                                                    REPOSITORY_LEAF_AK);

    private static final String REPOSITORY_GROUP_AA = "group-repo-aa";

    private static final String REPOSITORY_GROUP_AB = "group-repo-ab";

    private static final String REPOSITORY_GROUP_AC = "group-repo-ac";

    private static final String REPOSITORY_GROUP_AF = "group-repo-af";

    private static final String REPOSITORY_GROUP_AH = "group-repo-ah";

    @Inject
    private MavenMetadataGroupRepositoryComponent mavenGroupRepositoryComponent;

    protected Set<MutableRepository> getRepositories()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AE, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AL, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AZ, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AD, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AG, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AK, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AA, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AB, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AC, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AF, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AH, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createLeaf(STORAGE0, REPOSITORY_LEAF_AE);
        createLeaf(STORAGE0, REPOSITORY_LEAF_AL);
        createLeaf(STORAGE0, REPOSITORY_LEAF_AZ);
        createLeaf(STORAGE0, REPOSITORY_LEAF_AD);
        createLeaf(STORAGE0, REPOSITORY_LEAF_AG);
        createLeaf(STORAGE0, REPOSITORY_LEAF_AK);

        createGroup(REPOSITORY_GROUP_AC, STORAGE0, REPOSITORY_LEAF_AE, REPOSITORY_LEAF_AZ);
        createGroup(REPOSITORY_GROUP_AB, STORAGE0, REPOSITORY_GROUP_AC, REPOSITORY_LEAF_AD, REPOSITORY_LEAF_AL);
        createGroup(REPOSITORY_GROUP_AA, STORAGE0, REPOSITORY_LEAF_AG, REPOSITORY_GROUP_AB);
        createGroup(REPOSITORY_GROUP_AF, STORAGE0, REPOSITORY_GROUP_AC, REPOSITORY_LEAF_AD, REPOSITORY_LEAF_AL);
        createGroup(REPOSITORY_GROUP_AH, STORAGE0, REPOSITORY_GROUP_AF, REPOSITORY_LEAF_AK);

        generateArtifact(REPOSITORY_LEAF_AL_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_AG_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_AD_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(REPOSITORY_LEAF_AK_BASEDIR.getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AL);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AG);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AD);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AK);

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
                             REPOSITORY_GROUP_AH,
                             new String[]{ REPOSITORY_LEAF_AD },
                             ".*(com|org)/artifacts/to/update/releases/update-group.*",
                             ROUTING_RULE_TYPE_DENIED);

        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AF, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AB, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AA, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AH, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AK, REPOSITORY_GROUP_AH, FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));
    }

    private void copyArtifactMetadata(String sourceRepositoryId,
                                      String destinationRepositoryId,
                                      String path)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(sourceRepositoryId);
        final Path sourcePath = repositoryPathResolver.resolve(repository, path);

        repository = storage.getRepository(destinationRepositoryId);
        final Path destinationPath = repositoryPathResolver.resolve(repository, path);
        FileUtils.copyFile(sourcePath.toFile(), destinationPath.toFile());
    }

    @Test
    public void whenMetadataWasUploadedInRepositoryAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible()
            throws Exception
    {
        Repository repository = configurationManager.getConfiguration()
                                                    .getStorage(STORAGE0)
                                                    .getRepository(REPOSITORY_LEAF_AD);

        Metadata metadata;

        // BEFORE
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AD, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AK, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AH, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        RepositoryFiles.delete(repositoryPathResolver.resolve(
                new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AF, Maven2LayoutProvider.ALIAS)))
                                                     .resolve(
                                                             "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                               false);
        RepositoryFiles.delete(repositoryPathResolver.resolve(
                new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AB, Maven2LayoutProvider.ALIAS)))
                                                     .resolve(
                                                             "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                               false);
        RepositoryFiles.delete(repositoryPathResolver.resolve(
                new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AA, Maven2LayoutProvider.ALIAS)))
                                                     .resolve(
                                                             "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                               false);

        try
        {
            metadata = mavenMetadataManager.readMetadata(
                    repositoryPathResolver.resolve(new Repository(
                                                           createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AF, Maven2LayoutProvider.ALIAS)),
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
                    repositoryPathResolver.resolve(new Repository(
                                                           createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AB, Maven2LayoutProvider.ALIAS)),
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
                    repositoryPathResolver.resolve(new Repository(
                                                           createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AA, Maven2LayoutProvider.ALIAS)),
                                                   "com/artifacts/to/update/releases/update-group"));

            fail("metadata SHOULD NOT exist");
        }
        catch (FileNotFoundException expected)
        {
            // do nothing, by design
        }

        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository,
                                                                       "com/artifacts/to/update/releases/update-group");
        // IMITATE THE EVENT
        mavenGroupRepositoryComponent.updateGroupsContaining(repositoryPath);

        // AFTER
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AD, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_AK, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AH, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AB, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_AA, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

}
