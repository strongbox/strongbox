package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

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
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenMetadataGroupRepositoryComponentOnAddTest
        extends BaseMavenGroupRepositoryComponentTest
{

    private static final String REPOSITORY_LEAF_O = "leaf-repo-o";

    private static final String REPOSITORY_LEAF_P = "leaf-repo-p";

    private static final String REPOSITORY_LEAF_R = "leaf-repo-r";

    private static final String REPOSITORY_LEAF_S = "leaf-repo-s";

    private static final String REPOSITORY_LEAF_T = "leaf-repo-t";

    private static final String REPOSITORY_LEAF_U = "leaf-repo-u";

    private static final String REPOSITORY_GROUP_W = "group-repo-w";

    private static final String REPOSITORY_GROUP_X = "group-repo-x";

    private static final String REPOSITORY_GROUP_Y = "group-repo-y";

    private static final String REPOSITORY_GROUP_Z = "group-repo-z";

    private static final String REPOSITORY_GROUP_ZQ = "group-repo-zq";


    protected Set<MutableRepository> getRepositories()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_O, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_P, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_R, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_S, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_T, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_U, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_W, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_X, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_Y, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_Z, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_ZQ, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createLeaf(STORAGE0, REPOSITORY_LEAF_O);
        createLeaf(STORAGE0, REPOSITORY_LEAF_P);
        createLeaf(STORAGE0, REPOSITORY_LEAF_R);
        createLeaf(STORAGE0, REPOSITORY_LEAF_S);
        createLeaf(STORAGE0, REPOSITORY_LEAF_T);
        createLeaf(STORAGE0, REPOSITORY_LEAF_U);

        createGroup(REPOSITORY_GROUP_Y, STORAGE0, REPOSITORY_LEAF_O, REPOSITORY_LEAF_R);
        createGroup(REPOSITORY_GROUP_X, STORAGE0, REPOSITORY_GROUP_Y, REPOSITORY_LEAF_S, REPOSITORY_LEAF_P);
        createGroup(REPOSITORY_GROUP_W, STORAGE0, REPOSITORY_LEAF_T, REPOSITORY_GROUP_X);
        createGroup(REPOSITORY_GROUP_Z, STORAGE0, REPOSITORY_GROUP_Y, REPOSITORY_LEAF_S, REPOSITORY_LEAF_P);
        createGroup(REPOSITORY_GROUP_ZQ, STORAGE0, REPOSITORY_GROUP_Z, REPOSITORY_LEAF_U);

        // whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_P).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        // whenAnArtifactWasDeletedAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_T).getAbsolutePath(),
                         "com.artifacts.to.delete.releases:delete-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_S).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1",
                                       "1.2.2" }
        );

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_LEAF_U).getAbsolutePath(),
                         "com.artifacts.to.update.releases:update-group",
                         new String[]{ "1.2.1" }
        );

        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_P);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_T);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_S);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_U);

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
        createRoutingRuleSet(REPOSITORY_GROUP_ZQ,
                             new String[]{ REPOSITORY_LEAF_S },
                             ".*(com|org)/artifacts/to/update/releases/update-group.*",
                             ROUTING_RULE_TYPE_DENIED);

        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_Z, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_X, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_W, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_P, REPOSITORY_GROUP_ZQ, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_U, REPOSITORY_GROUP_ZQ, FilenameUtils.normalize(
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
    public void generationOfMavenMetadataInLeafsShouldResultUpToDateMetadataInGroups()
            throws Exception
    {
        Metadata metadata = mavenMetadataManager.readMetadata(repositoryPathResolver.resolve(new Repository(
                                                                                                     createRepositoryMock(STORAGE0,
                                                                                                                          REPOSITORY_LEAF_S, Maven2LayoutProvider.ALIAS)),
                                                                                             "com/artifacts/to/update/releases/update-group"));

        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_LEAF_U, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_ZQ, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_X, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(
                        new Repository(createRepositoryMock(STORAGE0, REPOSITORY_GROUP_W, Maven2LayoutProvider.ALIAS)),
                        "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }

}
